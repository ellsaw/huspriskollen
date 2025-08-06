package com.scraper.model.EstateData;

public class House extends Estate{
    int gardenAreaMetresSquared;

    public House(String estateType, double latitude, double longitude, int price, int livingAreaMetresSquared, int numberOfRooms, int ageYears, int maintenanceCostPerMonth, String[] tags, int gardenAreaMetresSquared){
        super(estateType, latitude, longitude, price, livingAreaMetresSquared, numberOfRooms, ageYears, maintenanceCostPerMonth, tags);
        this.gardenAreaMetresSquared = gardenAreaMetresSquared;
    }

    public void write() {
        
    }
}
