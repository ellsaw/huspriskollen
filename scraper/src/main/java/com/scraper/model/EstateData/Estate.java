package com.scraper.model.EstateData;

public class Estate {
    protected String estateType;
    protected double latitude;
    protected double longitude;
    protected int price;
    protected int livingAreaMetresSquared;
    protected int numberOfRooms;
    protected int ageYears;
    protected int maintenanceCostPerMonth;
    protected String[] tags;
    

    protected Estate(String estateType, double longitude, double latitude, int price, int livingAreaMetresSquared, int numberOfRooms, int ageYears, int maintenanceCostPerMonth, String[] tags) {
        this.estateType = estateType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.livingAreaMetresSquared = livingAreaMetresSquared;
        this.numberOfRooms = numberOfRooms;
        this.ageYears = ageYears;
        this.price = price;
        this.maintenanceCostPerMonth = maintenanceCostPerMonth;
        this.tags = tags;
    }

}


