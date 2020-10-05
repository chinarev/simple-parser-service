package com.wine.to.up.simple.parser.service.SimpleParser;

import java.io.IOException;
import java.util.ArrayList;

import com.wine.to.up.simple.parser.service.domain.entity.*;
import com.wine.to.up.simple.parser.service.repository.*;
import lombok.SneakyThrows;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Parser {
    private final String URL = "https://simplewine.ru";
    private final int PAGES_TO_PARSE = 108; // currently max 132, lower const value for testing purposes
    private Document doc;
    private Document wineDocument;
    private ArrayList<String> wineURLs;
    private ArrayList<SimpleWine> wineCatalog;
    private int numberOfPages;

    private String wineName = "";
    private String brandID = "";
    private String countryID = "";
    private float bottleVolume = 0;
    private String bottlePrice = "";
    private String bottleDiscount = "";
    private String bottleYear = "";
    private float bottleABV = 0;
    private String colorType = "";
    private String sugarType = "";
    private String grapeType = "";

    private SimpleWine newWine;

    @Autowired
    private GrapesRepository grapesRepository;
    @Autowired
    private BrandsRepository brandsRepository;
    @Autowired
    private CountriesRepository countriesRepository;
    @Autowired
    private WineGrapesRepository wineGrapesRepository;
    @Autowired
    private WineRepository wineRepository;

    public Parser() {
        wineURLs = new ArrayList<>();
        wineCatalog = new ArrayList<>();
    }

    @SneakyThrows(IOException.class)
    public ArrayList<SimpleWine> startParser() {
        doc = Jsoup.connect(URL + "/catalog/vino/").get();
        numberOfPages = Integer
                .parseInt(doc.getElementsByAttributeValue("class", "pagination__navigation").get(0).child(7).text());

        log.info("\tStart of adding information to the database.");
        for (int i = 1; i < PAGES_TO_PARSE; i++) {
            doc = Jsoup.connect(URL + "/catalog/vino/page" + i).get();
            Elements wines = doc.getElementsByAttributeValue("class", "catalog-grid__item");

            for (Element wine : wines) {
                String[] NameAndYear = wine.getElementsByAttributeValue("class", "product-snippet__name").text()
                        .split(", ");
                this.wineName = NameAndYear[0].replaceFirst("Вино ", "");
                this.bottlePrice = wine.getElementsByAttributeValue("class", "product-snippet__price").attr("content");

                Elements wine_Elements = wine.getElementsByAttributeValue("class", "product-snippet__info-item");

                for (Element wine_Element : wine_Elements) {

                    switch (wine_Element.child(0).text()) {
                        case "Страна:":
                            this.countryID = wine_Element.child(1).text();
                            break;
                        case "Цвет:":
                            this.colorType = wine_Element.child(1).text();
                            break;
                        case "Сахар:":
                            this.sugarType = wine_Element.child(1).text();
                            break;
                        case "Объем:":
                            this.bottleVolume = Float.parseFloat(wine_Element.child(1).text().split(" ")[0]);
                            break;
                        case "Производитель:":
                            this.brandID = wine_Element.child(1).text();
                            break;
                        case "Виноград:":
                            this.grapeType = wine_Element.child(1).text();
                            break;

                        default:
                            break;

                    }
                }
                wineURLs.add(wine.getElementsByAttributeValue("class", "product-snippet__name").attr("href"));

                newWine = new SimpleWine(wineName, brandID, countryID, bottlePrice, bottleVolume, bottleABV, colorType,
                        sugarType);
                wineCatalog.add(newWine);

                newWine.writeInfoToFile();

                //A price check. bottlePrice may be empty if the wine is not in stock
                if (!bottlePrice.isEmpty())
                    putInfoToDB();
            }
        }
        log.info("\tEnd of adding information to the database.");
        return wineCatalog;
    }

    private void putInfoToDB() {
        Countries countryEntity;
        if (!countriesRepository.existsCountriesByCountryName(this.countryID)) {
            countryEntity = new Countries(this.countryID);
            countriesRepository.save(countryEntity);
            log.info("New Country was added to DB: " + countryID);
        } else
            countryEntity = countriesRepository.findCountryByCountryName(this.countryID);

        Brands brandEntity;
        if (!brandsRepository.existsBrandsByBrandName(this.brandID)) {
            brandEntity = new Brands(this.brandID);
            brandsRepository.save(brandEntity);
            log.info("New Brand was added to DB: " + brandID);
        } else
            brandEntity = brandsRepository.findBrandByBrandName(this.brandID);

        Grapes grapeEntity;
        if (!grapesRepository.existsGrapesByGrapeName(this.grapeType)) {
            grapeEntity = new Grapes(this.grapeType);
            grapesRepository.save(grapeEntity);
            log.info("New Grape was added to DB: " + grapeType);
        } else
            grapeEntity = grapesRepository.findGrapeByGrapeName(this.grapeType);

        Float price = Float.parseFloat(this.bottlePrice.replace(" ", "").replace("₽", ""));
        //int year = Integer.parseInt(this.bottleYear.replace(" г.", ""));
        //int discount = Integer.parseInt(this.bottleDiscount.replace("-","").replace("%", ""));
        Wine wineEntity;
        if(!wineRepository.existsWineByNameAndPriceAndVolumeAndColorTypeAndSugarType(this.wineName, price, this.bottleVolume, this.colorType, this.sugarType)){
            wineEntity = new Wine(this.wineName, brandEntity, countryEntity, price,
                    this.bottleVolume, this.bottleABV, this.colorType, this.sugarType, this.grapeType);
            wineRepository.save(wineEntity);
            log.info("New Wine was added to DB: " + wineEntity.toString());
        } else
            wineEntity = wineRepository.findWineByNameAndPriceAndVolumeAndColorTypeAndSugarType(this.wineName, price, this.bottleVolume, this.colorType, this.sugarType);


        WineGrapes wineGrapeEntity;
        if (!wineGrapesRepository.existsWineGrapesByGrapeIdAndAndWineId(grapeEntity, wineEntity)) {
            wineGrapeEntity = new WineGrapes(wineEntity, grapeEntity);
            wineGrapesRepository.save(wineGrapeEntity);
            log.info("New Connection between Wine and Grape was added to DB");
        }
    }
}