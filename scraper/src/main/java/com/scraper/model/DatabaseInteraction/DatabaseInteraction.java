package com.scraper.model.DatabaseInteraction;

import java.time.Year;

import com.google.gson.JsonObject;

public class DatabaseInteraction {
    public static void Write(String resedentialType, JsonObject propertyInfo) {
        try {
            double latitude = propertyInfo.get("latitude").getAsDouble();
            double longitude = propertyInfo.get("longitude").getAsDouble();

            JsonObject soldPriceJson = propertyInfo.getAsJsonObject("soldPrice");
            int price = soldPriceJson.get("raw").getAsInt();

            JsonObject livingAreaJson = propertyInfo.getAsJsonObject("livingArea");
            int livingAreaMetresSquared = livingAreaJson.get("raw").getAsInt();

            int ageYears = Year.now().getValue() - propertyInfo.get("constructionYear").getAsInt();

            JsonObject operatingCostJson = propertyInfo.getAsJsonObject("operatingCost");
            int maintenanceCostPerMonth = operatingCostJson.get("raw").getAsInt();


            switch (resedentialType) {
                case "Villa":

                    int additionalAreaMetresSquared = 0;
                    if(propertyInfo.get("additionalArea").isJsonObject()){
                        JsonObject additionalAreaJson = propertyInfo.getAsJsonObject("additionalArea");

                        additionalAreaMetresSquared = additionalAreaJson.get("raw").getAsInt();
                    }

                    int plotAreaMetresSquared = 0;
                    if(propertyInfo.get("plotArea").isJsonObject()){
                        JsonObject plotAreaJson = propertyInfo.getAsJsonObject("plotArea");

                        plotAreaMetresSquared = plotAreaJson.get("raw").getAsInt();
                    }

                    PostgreSQLGateway sqlGateway = PostgreSQLGateway.initialise(resedentialType);

                    sqlGateway.writeVilla(latitude, longitude, price, livingAreaMetresSquared, ageYears, maintenanceCostPerMonth, additionalAreaMetresSquared, plotAreaMetresSquared);

                    break;
            
                default:
                    throw new IllegalArgumentException("Invalid residential type");
            }


        } catch (Exception e) {
            if(e instanceof java.lang.ClassCastException || e instanceof java.lang.UnsupportedOperationException){
                System.err.println("Missing critical information");
            }else{
                System.err.println(e);
            }

        }
    }
}
