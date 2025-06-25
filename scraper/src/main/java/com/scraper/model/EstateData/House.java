package com.scraper.model.EstateData;

import java.util.HashMap;
import java.util.Map;

public class House extends Estate{
    int gardenAreaMetresSquared;

    public House(String estateType, int price, String adress, int livingAreaMetresSquared, int numberOfRooms, int ageYears, int maintenanceCostPerMonth, String[] tags, int gardenAreaMetresSquared){
        super(estateType, price, adress, livingAreaMetresSquared, numberOfRooms, ageYears, maintenanceCostPerMonth, tags);
        this.gardenAreaMetresSquared = gardenAreaMetresSquared;
    }

    public Map<String, Object> get(){
        Map<String, Object> data = new HashMap<>();
        data.put("estateType", estateType);
        data.put("adress", adress);
        data.put("livingAreaMetresSquared", livingAreaMetresSquared);
        data.put("numberOfRooms", numberOfRooms);
        data.put("ageYears", ageYears);
        data.put("maintenanceCostPerMonth", maintenanceCostPerMonth);
        data.put("tags", tags);
        data.put("gardenAreaMetresSquared", gardenAreaMetresSquared);
        return data;
    }
}
