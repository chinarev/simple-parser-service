package com.wine.to.up.simple.parser.service.SimpleParser;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ShopCatalog {

    private int wineID;
    private int wineShopID;
    private int shopID;
    private int price;
    private WineShop newShop;


    public void writeToFile(String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath, true)) {
            fileWriter.write(this.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

