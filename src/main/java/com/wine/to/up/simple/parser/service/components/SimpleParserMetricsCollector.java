package com.wine.to.up.simple.parser.service.components;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.wine.to.up.commonlib.metrics.CommonMetricsCollector;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Metrics;
import io.prometheus.client.Summary;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

/**
 * This Class expose methods for recording specific metrics It changes metrics
 * of Micrometer and Prometheus simultaneously Micrometer's metrics exposed at
 * /actuator/prometheus Prometheus' metrics exposed at /metrics-prometheus
 */
@Component
public class SimpleParserMetricsCollector extends CommonMetricsCollector {
    private static final String SERVICE_NAME = "simple_parser_service";

    private static final String WINE_DETAILS_FETCHING_DURATION = "wine_details_fetching_duration";
    private static final String PARSING_PROCESS_DURATION = "parsing_process_duration";
    private static final String TIME_SINCE_LAST_SUCCEEDED_PARSING = "time_since_last_succeeded_parsing";
    private static final String PARSING_STARTED_TOTAL = "parsing_started_total";
    private static final String PARSING_COMPLETE_TOTAL = "parsing_complete_total";
    private static final String PARSING_IN_PROGRESS = "parsing_in_progress";
    private static final String WINE_PAGE_FETCHING_DURATION = "wine_page_fetching_duration";
    private static final String WINE_DETAILS_PARSING_DURATION = "wine_details_parsing_duration";
    private static final String WINE_PAGE_PARSING_DURATION = "wine_page_parsing_duration";
    private static final String WINES_PUBLISHED_TO_KAFKA_COUNT = "wines_published_to_kafka_count";

    public SimpleParserMetricsCollector() {
        this(SERVICE_NAME);
    }

    public SimpleParserMetricsCollector(String serviceName) {
        super(serviceName);
    }

    private static final Summary parsePageWineFetchSummary = Summary.build()
            .name(WINE_PAGE_FETCHING_DURATION)
            .help("Wine page fetching time")
            .register();

    private static final Summary parseDetailsFetchSummary = Summary.build()
            .name(WINE_DETAILS_FETCHING_DURATION)
            .help("Wine details page fetching time")
            .register();

    private static final Summary parseWineDetailsParsingSummary = Summary.build()
            .name(WINE_DETAILS_PARSING_DURATION)
            .help("Parsing wine details page time")
            .register();

    private static final Summary winePageParsingDurationSummary = Summary.build()
            .name(WINE_PAGE_PARSING_DURATION)
            .help("Wine catalog page parsing duration")
            .register();

    private static final Counter winesPublishedToKafkaCount = Counter.build()
            .name(WINES_PUBLISHED_TO_KAFKA_COUNT)
            .help("Wines published to kafka count")
            .register();

    private static final Counter parsingStartedTotal = Counter.build()
            .name(PARSING_STARTED_TOTAL)
            .help("Number of started parsings")
            .register();

    private static final Counter parsingCompletedTotal = Counter.build()
            .name(PARSING_COMPLETE_TOTAL)
            .help("Number of completed parsings")
            .labelNames("status")
            .register();

    private static Gauge parsingInProgress = Gauge.build()
            .name(PARSING_IN_PROGRESS)
            .help("Number of parsers in progress")
            .register();

    private static final AtomicInteger micrometerParsingInProgressGauge = Metrics.gauge(PARSING_IN_PROGRESS, new AtomicInteger(0));
    private static final AtomicLong micrometerTimeSinceLastSucceededParsingGauge = Metrics.gauge(TIME_SINCE_LAST_SUCCEEDED_PARSING, new AtomicLong(0));

    private static final Summary parseProcessSummary = Summary.build()
            .name(PARSING_PROCESS_DURATION)
            .help("Total parsing process time")
            .register();

    private static final Gauge lastSucceededParseGauge = Gauge.build()
            .name(TIME_SINCE_LAST_SUCCEEDED_PARSING)
            .help("Time since last succeeded parsing")
            .register();

    public static void parseProcess(long time) {
        Metrics.timer(PARSING_PROCESS_DURATION).record(time, TimeUnit.MILLISECONDS);
        parseProcessSummary.observe(time);
    }

    public static void fetchDetailsWine(long time) {
        Metrics.timer(WINE_DETAILS_FETCHING_DURATION).record(time, TimeUnit.MILLISECONDS);
        parseDetailsFetchSummary.observe(time);
    }

    public static void fetchWinePage(long time) {
        Metrics.timer(WINE_PAGE_FETCHING_DURATION).record(time, TimeUnit.MILLISECONDS);
        parsePageWineFetchSummary.observe(time);
    }

    public static void timeSinceLastSucceededParse(long time) {
        micrometerTimeSinceLastSucceededParsingGauge.set(time);
        lastSucceededParseGauge.set(time);
    }

    public static void recordParsingStarted() {
        Metrics.counter(PARSING_STARTED_TOTAL).increment();
        micrometerParsingInProgressGauge.incrementAndGet();
        parsingInProgress.inc();
        parsingStartedTotal.inc();
    }

    public static void recordParsingCompleted(boolean status) {
        Metrics.counter(PARSING_COMPLETE_TOTAL, "status", status ? "SUCCESS" : "FAILED").increment();
        micrometerParsingInProgressGauge.decrementAndGet();
        parsingInProgress.dec();
        parsingCompletedTotal.labels(status ? "SUCCESS" : "FAILED").inc();
        timeSinceLastSucceededParse(System.currentTimeMillis());
    }

    public static void parseWineDetailsParsing(long time) {
        Metrics.timer(WINE_DETAILS_PARSING_DURATION).record(time, TimeUnit.MILLISECONDS);
        parseWineDetailsParsingSummary.observe(time);
    }

    public static void winePageParsingDuration(long time) {
        Metrics.timer(WINE_PAGE_PARSING_DURATION).record(time, TimeUnit.MILLISECONDS);
        winePageParsingDurationSummary.observe(time);

    }

    public static void winesPublishedToKafka() {
        Metrics.counter(WINES_PUBLISHED_TO_KAFKA_COUNT).increment();
        winesPublishedToKafkaCount.inc();
    }

}
