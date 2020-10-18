package com.wine.to.up.simple.parser.service.SimpleParser;

import java.io.IOException;

import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class Parser {
    private static String URL;
    private static final int PAGES_TO_PARSE = 108; // currently max 132, lower const value for testing purposes
    public static String HOME_URL;
    private static String WINE_URL;

    @Value("${parser.url}")
    public void setURLStatic(String URL_FROM_PROPERTY) {
        URL = URL_FROM_PROPERTY;
        HOME_URL = URL + "/catalog/vino/";
        WINE_URL = URL + "/catalog/vino/page";
    }

    public static Document URLToDocument(String someURL) throws IOException {
        return Jsoup.connect(someURL).get();
    }

    protected int parseNumberOfPages(Document mainPage) {
        return Integer.parseInt(
                //mainPage.getElementsByAttributeValue("class", "pagination__navigation").get(0).child(7).text()); //works only for catalogs with more than 7 pages
                mainPage.getElementsByAttributeValue("class", "pagination__navigation").get(0).children().last().previousElementSibling().text()); //works for catalogs with 7 or less pages
    }

    public static SimpleWine parseWine(Document wineDoc) {
        String wineName = "";
        String brandID = "";
        String countryID = "";
        float bottleVolume = 0;
        float bottlePrice = 0;
        float bottleDiscount = 0;
        int bottleYear = 0;
        float bottleABV = 0;
        String colorType = "";
        String sugarType = "";
        String grapeType = "";
        String region = "";

        wineName = wineDoc.getElementsByClass("product__header-russian-name").get(0).text();
        Elements prices = wineDoc.getElementsByClass("product__buy-price");
        if (prices.get(0).childrenSize() > 1) {
            bottlePrice = Float.parseFloat(prices.get(0).child(1).text().replaceAll(" |₽", ""));
            bottleDiscount = Float.parseFloat(prices.get(0).child(2).text().replaceAll("-|%", ""));
        } else {
            bottlePrice = Float.parseFloat(prices.get(0).child(0).text().replaceAll(" |₽", ""));
            bottleDiscount = 0;
        }

        Elements productFacts = wineDoc.getElementsByClass("product__facts-info-text");
        for (Element productFact : productFacts) {
            if (productFact.childrenSize() > 0) {
                String href = productFact.child(0).attr("href");
                String fact = href.split("/")[4].split("(-|_)")[0];
                switch (fact) {
                    case "country":
                        countryID = productFact.text().split(",")[0];
                        break;
                    case "color":
                        colorType = productFact.text();
                        break;
                    case "sugar":
                        sugarType = productFact.text();
                        break;
                    case "grape":
                        grapeType = productFact.text();
                        break;
                    case "aging":
                        // grapeType = productFact.text();
                        break;

                    default:
                        break;

                }
            }
        }

        Elements productCharateristics = wineDoc.getElementsByClass("characteristics-params__item");
        for (Element productCharateristic : productCharateristics) {
            String charateristicTitle = productCharateristic.child(0).text();
            // System.out.println(charateristicTitle);
            switch (charateristicTitle) {
                case "Регион:":
                    region = productCharateristic.child(1).text();
                    break;
                case "Производитель:":
                    brandID = productCharateristic.child(1).text();
                    break;
                case "Объем:":
                    bottleVolume = Float.parseFloat(productCharateristic.child(1).text());
                    break;
                case "Год:":
                    bottleYear = Integer.parseInt(productCharateristic.child(1).text());
                    break;
                case "Крепость:":
                    bottleABV = Float.parseFloat(productCharateristic.child(1).text().replaceAll("%", ""));
                    break;

                default:
                    break;

            }
        }

        return SimpleWine.builder().name(wineName).brandID(brandID).countryID(countryID).price(bottlePrice)
                .year(bottleYear).volume(bottleVolume).abv(bottleABV).colorType(colorType).grapeType(grapeType)
                .sugarType(sugarType).discount(bottleDiscount).build();

    }
}
