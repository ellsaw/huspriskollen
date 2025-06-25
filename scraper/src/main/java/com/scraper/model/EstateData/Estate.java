package com.scraper.model.EstateData;

public class Estate {
    protected String estateType;
    protected int price;
    protected String adress;
    protected int livingAreaMetresSquared;
    protected int numberOfRooms;
    protected int ageYears;
    protected int maintenanceCostPerMonth;
    protected String[] tags;
    

    protected Estate(String estateType, int price, String adress, int livingAreaMetresSquared, int numberOfRooms, int ageYears, int maintenanceCostPerMonth, String[] tags) {
        this.estateType = estateType;
        this.adress = adress;
        this.livingAreaMetresSquared = livingAreaMetresSquared;
        this.numberOfRooms = numberOfRooms;
        this.ageYears = ageYears;
        this.price = price;
        this.maintenanceCostPerMonth = maintenanceCostPerMonth;
        this.tags = tags;
    }

}


