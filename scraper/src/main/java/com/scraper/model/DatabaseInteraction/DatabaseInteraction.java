package com.scraper.model.DatabaseInteraction;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonObject;

public class DatabaseInteraction {
    private static int getUnix(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        return (int) localDateTime.toEpochSecond(ZoneOffset.UTC);
    }

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

            int dateUnix = getUnix(propertyInfo.get("removed").getAsString());

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

                    sqlGateway.writeVilla(latitude, longitude, price, livingAreaMetresSquared, ageYears, maintenanceCostPerMonth, additionalAreaMetresSquared, plotAreaMetresSquared, dateUnix);

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
