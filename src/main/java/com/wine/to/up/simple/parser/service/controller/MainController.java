package com.wine.to.up.simple.parser.service.controller;

import com.wine.to.up.simple.parser.service.domain.entity.*;
import com.wine.to.up.simple.parser.service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping(path = "/simple-parser")
public class MainController {

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


    @PostMapping(path="/grape")
    @ResponseBody
    public String addGrape(@RequestParam String grapeName){
        grapesRepository.save(new Grapes(grapeName));
        return "New Grape Added";
    }

    @PostMapping(path="/brand")
    @ResponseBody
    public String addBrand(@RequestParam String brandName){
        brandsRepository.save(new Brands(brandName));
        return "New Brand Added";
    }

    @PostMapping(path="/country")
    @ResponseBody
    public String addCountry(@RequestParam String countryName){
        countriesRepository.save(new Countries(countryName));
        return "New Country Added";
    }

    @PostMapping(path="/wine")
    @ResponseBody
    public String addWine(@RequestParam String name, @RequestParam String brandS, @RequestParam String countryS, @RequestParam float price,
                          @RequestParam Float volume, @RequestParam Float abv, @RequestParam String colorType,
                          @RequestParam String sugarType, @RequestParam List<String> wineGrapes, @RequestParam int discount, @RequestParam int year){

        Brands brand=brandsRepository.findBrandByBrandName(brandS);
        Countries country=countriesRepository.findCountryByCountryName(countryS);
        Wine newWine = new Wine(name, brand, country, price, discount, volume, abv, year, colorType, sugarType, wineGrapes.toString());
        wineRepository.save(newWine);

        for(String someGrape: wineGrapes){
            wineGrapesRepository.save(new WineGrapes(newWine, grapesRepository.findGrapeByGrapeName(someGrape)));
        }

        return "New Wine Added";
    }

    @GetMapping(path="/all-grapes")
    @ResponseBody
    public String getAllGrapes() {
        Iterable<Grapes> grapes =  grapesRepository.findAll();
        String html = "";
        for (Grapes someGrape : grapes) {
            html += someGrape + "<br>";
        }

        return html;
    }

    @GetMapping(path="/all-brands")
    @ResponseBody
    public String getAllBrands() {
        Iterable<Brands> brands =  brandsRepository.findAll();
        String html = "";
        for (Brands someBrand : brands) {
            html += someBrand + "<br>";
        }

        return html;
    }

    @GetMapping(path="/all-countries")
    @ResponseBody
    public String getAllCountries() {
        Iterable<Countries> countries =  countriesRepository.findAll();
        String html = "";
        for (Countries someCountry : countries) {
            html += someCountry + "<br>";
        }
        return html;
    }

//    @GetMapping(path="/all-wine-grapes")
//    @ResponseBody
//    public Iterable<WineGrapesInfo> getAllWineGrapesInfo() {
//        return wineGrapesRepository.findAll();
//    }

    @GetMapping(path="/all-wines")
    @ResponseBody
    public String getAllWines() {
        Iterable<Wine> wines =  wineRepository.findAll();
        String html = "";
        for (Wine someWine : wines) {
            html += someWine + "<br>";
        }
        return html;
    }

    @ResponseBody
    @GetMapping(path = "/")
    public String home() {
        String html = "";
        html += "<ul>";
        html += " <li><a href='/simple-parser/all-wines'>Show All Wines</a></li>";
        html += " <li><a href='/simple-parser/all-countries'>Show All Countries</a></li>";
        html += " <li><a href='/simple-parser/all-brands'>Show All Brands</a></li>";
        html += " <li><a href='/simple-parser/all-grapes'>Show All Grapes</a></li>";
        html += "</ul>";
        return html;
    }
}