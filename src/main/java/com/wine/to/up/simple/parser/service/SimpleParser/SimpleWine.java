package com.wine.to.up.simple.parser.service.SimpleParser;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

//@ToString
@NoArgsConstructor
public class SimpleWine {
    private UUID wineID = UUID.randomUUID();
    private File picture;
    private String brandID;
    private String countryID;
    private Float volume;
    private Float abv; // alcohol by volume
    private String colorType;
    private String sugarType;
    private int year;
    private String price;
    private String name;

    public SimpleWine(String name, String brandID, String countryID, String price, Float volume, Float abv, String colorType,
                      String sugarType) {
        this.name = name;
        this.brandID = brandID;
        this.price = price;
        this.countryID = countryID;
        this.volume = volume;
        this.abv = abv;
        this.colorType = colorType;
        this.sugarType = sugarType;
    }

    @SneakyThrows(IOException.class)
    public void writeInfoToFile() {
        FileWriter writer = new FileWriter("Wine.txt", true);
        writer.write(this.toString() + "\n");
        writer.flush();
        writer.close();
    }

    @Override
    public String toString() {
        return "{name=" + this.name + ", brandID=" + this.brandID + ", countryID=" + this.countryID + ", price="
                + this.price + ", volume=" + this.volume + ", abv=" + this.abv + ", colorType=" + this.colorType
                + ", sugarType=" + this.sugarType + '}';
    }
}
