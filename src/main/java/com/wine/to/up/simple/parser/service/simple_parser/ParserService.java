package com.wine.to.up.simple.parser.service.simple_parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.wine.to.up.commonlib.annotations.InjectEventLogger;
import com.wine.to.up.commonlib.logging.EventLogger;
import com.wine.to.up.commonlib.messaging.KafkaMessageSender;
import com.wine.to.up.parser.common.api.schema.ParserApi;
import com.wine.to.up.simple.parser.service.components.SimpleParserMetricsCollector;
import com.wine.to.up.simple.parser.service.simple_parser.db_handler.WineService;
import com.wine.to.up.simple.parser.service.simple_parser.mappers.WineMapper;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.wine.to.up.simple.parser.service.logging.SimpleParserNotableEvents.*;

@Slf4j
@Service
public class ParserService {
    @InjectEventLogger
    public static EventLogger eventLogger;
    @Value("${parser.url}")
    private String url;
    @Value("${parser.wineurl}")
    private String wineUrl;
    @Value("${parser.sparkling_wineurl}")
    private String sparklingWineUrl;
    private static final int NUMBER_OF_THREADS = 15;
    private ParserApi.WineParsedEvent messageToKafka;
    private final ExecutorService pagesExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService winesExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private final KafkaMessageSender<ParserApi.WineParsedEvent> kafkaSendMessageService;
    private final WineService wineService;
    private final WineMapper wineMapper;

    public ParserService(KafkaMessageSender<ParserApi.WineParsedEvent> kafkaSendMessageService, WineService wineService, WineMapper wineMapper) {
        this.kafkaSendMessageService = kafkaSendMessageService;
        this.wineService = wineService;
        this.wineMapper = wineMapper;
    }

    /**
     * Creating Jsoup Document from URL
     *
     * @param someURL URL to get jsoup Document
     * @return Jsoup Document class
     */
    public static Document urlToDocument(String someURL) {
        Document wineDoc = null;
        try {
            wineDoc = Jsoup.connect(someURL).get();
            if (wineDoc.is(":has(.product-page)")) {
                int rerequestNumber = 0;
                while ((rerequestNumber < 3) && !wineDoc.getElementsByClass("product-page").first().children().first().className().equals("container")) {
                    log.debug("Doing re-request: {}", rerequestNumber);
                    wineDoc = Jsoup.connect(someURL).get();
                    rerequestNumber++;
                }
            }
        } catch (IOException e) {
            log.error("Incorrect URL address: " + someURL);
        }
        return wineDoc;
    }

    /**
     * Parsing wines, collecting and sending to Kafka
     *
     * @param pagesToParse number pages to parse
     */
    private void parser(int pagesToParse, int sparklingPagesToParse) {
        SimpleParserMetricsCollector.recordParsingStarted();
        long start = System.currentTimeMillis();

        List<CompletableFuture<?>> futures = new ArrayList<>();
        ArrayBlockingQueue<String> wineURLs = new ArrayBlockingQueue<>(100_000);
        List<ParserApi.Wine> products = new ArrayList<>();

        AtomicLong pageCounter = new AtomicLong(1);
        AtomicLong sparklingPageCounter = new AtomicLong(1);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> addWineUrls(pageCounter, sparklingPageCounter, wineURLs, pagesToParse, sparklingPagesToParse), pagesExecutor);
            futures.add(future);
        }

        AtomicInteger wineCounter = new AtomicInteger(1);
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    while (true) {
                        String wineURL = wineURLs.poll(10, TimeUnit.SECONDS);
                        if (wineURL == null) {
                            return;
                        }
                        addWineToProducts(wineURL, products, wineService, wineCounter);
                    }
                } catch (InterruptedException e) {
                    log.error("Interrupt ", e);
                    Thread.currentThread().interrupt();
                }
            }, winesExecutor);
            futures.add(future);
        }
        futures.forEach(CompletableFuture::join);
        log.info("End of adding information to the database.");
        generateMessageToKafka(products);
        log.info("TIME : {} min {} seconds", TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()
                        - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start) * 60000 - start));
        SimpleParserMetricsCollector.parseProcess(System.currentTimeMillis() - start);
        log.info("End of parsing, {} wines collected and sent to Kafka", products.size());
        SimpleParserMetricsCollector.recordParsingCompleted(true);
    }

    /**
     * Multithreading simplewine parser with a specified number of pages
     */
    public void startParser(int pagesToParse, int sparklingPagesToParse) {
        if (pagesToParse <= Parser.parseNumberOfPages(urlToDocument(url + "/catalog/vino/"))
                && sparklingPagesToParse <= Parser.parseNumberOfPages(urlToDocument(url + "/catalog/shampanskoe_i_igristoe_vino/")) &&
                (sparklingPagesToParse > 0 || pagesToParse > 0)) {
            parser(pagesToParse, sparklingPagesToParse);
        } else {
            log.error("Set invalid number of pages: {}", pagesToParse);
            SimpleParserMetricsCollector.recordParsingCompleted(false);
        }
    }

    /**
     * Multithreading simplewine parser with maximum number of pages
     */
    public void startParser() {
        parser(Parser.parseNumberOfPages(urlToDocument(url + "/catalog/vino/")), Parser.parseNumberOfPages(urlToDocument(url + "/catalog/shampanskoe_i_igristoe_vino/")));
    }

    /**
     * Getting of all products
     *
     * @return Message to Kafka
     */
    public ParserApi.WineParsedEvent getMessage() {
        return messageToKafka;
    }

    /**
     * Adding wine to the Product list
     *
     * @param wineURL
     * @param products
     * @param dbHandler
     * @param wineCounter
     */
    private void addWineToProducts(String wineURL, List<ParserApi.Wine> products, WineService dbHandler, AtomicInteger wineCounter) {
        Document wineDocument = urlToDocument(url + wineURL);
        if (wineDocument != null && wineDocument.getElementsByClass("product-page").first().children().first().className().equals("container")) {
            SimpleWine wine = Parser.parseWine(wineDocument);
            saveWineToDB(wine, dbHandler);
            ParserApi.Wine newProduct = wineMapper.toKafka(wine).build();
            if (!products.contains(newProduct)) {
                products.add(newProduct);
                SimpleParserMetricsCollector.winesPublishedToKafka();
                eventLogger.info(I_WINE_DETAILS_PARSED, url + wineURL);
            }
            log.trace("Wine: {} added to database", wineCounter.getAndIncrement());
        } else {
            eventLogger.warn(W_WINE_DETAILS_PARSING_FAILED, url + wineURL);
        }
    }

    /**
     * Saving all parsed wines info to database
     *
     * @param wine
     * @param dbHandler
     */
    private void saveWineToDB(SimpleWine wine, WineService dbHandler) {
        try {
            if ((wine.getBrand() != null) && (wine.getCountry() != null)
                    && !(wine.getBrand().equals(""))) {
                Thread.sleep((long) (Math.random() * 1500));
                dbHandler.saveAllWineParsedInfo(wine);
            }
        } catch (Exception e) {
            log.error("DB error ", e);
            eventLogger.warn(W_SOME_WARN_EVENT, e.getMessage() + " caused by: " + e.getCause());
        }
    }

    /**
     * Collecting all wine URLs
     *
     * @param pageCounter
     * @param wineURLs
     * @param pagesToParse
     */
    private void addWineUrls(AtomicLong pageCounter, AtomicLong sparklingPageCounter, ArrayBlockingQueue<String> wineURLs, int pagesToParse, int sparklingPagesToParse) {
        parseUrls(pageCounter, pagesToParse, wineURLs);
        parseUrls(sparklingPageCounter, sparklingPagesToParse, wineURLs);
    }

    private void parseUrls(AtomicLong pageCounter, int pagesToParse, ArrayBlockingQueue<String> wineURLs) {
        try {
            long winePageCatalogParseStart = System.currentTimeMillis();
            while (pageCounter.longValue() <= pagesToParse) {
                long wineParseStart = System.currentTimeMillis();
                Document doc = Jsoup.connect(wineUrl + pageCounter.get()).get();
                SimpleParserMetricsCollector.fetchDetailsWine(new Date().getTime() - wineParseStart);
                Elements wines = doc.getElementsByClass("catalog-grid__item");
                for (Element wine : wines) {
                    if (!wineURLs.contains(wine.getElementsByClass("product-snippet__name").attr("href")))
                        wineURLs.add(wine.getElementsByClass("product-snippet__name").attr("href"));
                }
                log.debug("Parsed {} wines from url {}", wines.size(),
                        wineUrl + pageCounter.get());
                eventLogger.info(I_WINES_PAGE_PARSED, wineUrl + pageCounter.getAndIncrement());
                SimpleParserMetricsCollector.winePageParsingDuration(new Date().getTime() - wineParseStart);
            }
            SimpleParserMetricsCollector.fetchWinePage(new Date().getTime() - winePageCatalogParseStart);
        } catch (IOException e) {
            log.error("Error while parsing page: ", e);
            eventLogger.warn(W_WINE_PAGE_PARSING_FAILED, wineUrl + pageCounter.get());
        }
    }

    /**
     * @param products
     */
    private void generateMessageToKafka(List<ParserApi.Wine> products) {
        if (products.isEmpty()) {
            log.error("\t Z E R O\tP A R S I N G");
        } else {
            ParserApi.WineParsedEvent.Builder message = ParserApi.WineParsedEvent.newBuilder();
            if (products.size() >= 1000) {
                int messageSize = (int) Math.round(products.size() / 4.0);
                for (int i = 0; i < 4; i++) {
                    if (i == 3)
                        message = message
                                .addAllWines(products.subList(i * messageSize, products.size() - 1));
                    else
                        message = message
                                .addAllWines(products.subList(i * messageSize, (i + 1) * messageSize - 1));
                }
            } else {
                message = message.addAllWines(products);
            }
            kafkaSendMessageService.sendMessage(message.setShopLink(url).setParserName("simple-parser-service").build());
            messageToKafka = ParserApi.WineParsedEvent.newBuilder().addAllWines(products).build();
        }
    }
}