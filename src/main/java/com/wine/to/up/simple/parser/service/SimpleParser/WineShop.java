package com.wine.to.up.simple.parser.service.SimpleParser;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@ToString
@Getter
@Slf4j
public class WineShop {
    private int shopID;
    private String shopName;
    private String shopAddress;
    private String shopPhone;
    private String shopWorkingHours;


    public WineShop(int shopID, String shopName, String shopAddress, String shopPhone, String shopWorkingHours) {
        this.shopID = shopID;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.shopPhone = shopPhone;
        this.shopWorkingHours = shopWorkingHours;
        log.info("New WineShop object was created");
        infoToLog();
    }


    //log object's current state
    public void infoToLog() {
        log.info("Current object state: " + this.toString());
    }


    //write object info to file
    public void infoToFile(File file) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(this.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setShopID(int shopID) {
        log.info("shopID was changed from \"{}\" to \"{}\"", this.shopID, shopID);
        this.shopID = shopID;
        infoToLog();
    }


    public void setShopName(String shopName) {
        log.info("shopName was changed from \"{}\" to \"{}\"", this.shopName, shopName);
        this.shopName = shopName;
        infoToLog();
    }


    public void setShopAddress(String shopAddress) {
        log.info("shopAddress was changed from \"{}\" to \"{}\"", this.shopAddress, shopAddress);
        this.shopAddress = shopAddress;
        infoToLog();
    }


    public void setShopPhone(String shopPhone) {
        log.info("shopPhone was changed from \"{}\" to \"{}\"", this.shopPhone, shopPhone);
        this.shopPhone = shopPhone;
        infoToLog();
    }


    public void setShopWorkingHours(String shopWorkingHours) {
        log.info("shopWorkingHours was changed from \"{}\" to \"{}\"", this.shopWorkingHours, shopWorkingHours);
        this.shopWorkingHours = shopWorkingHours;
        infoToLog();
    }
}
