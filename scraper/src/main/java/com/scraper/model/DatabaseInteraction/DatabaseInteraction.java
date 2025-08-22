package com.scraper.model.DatabaseInteraction;

import java.time.Year;

import org.openqa.selenium.InvalidArgumentException;

import com.google.gson.JsonElement;
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

/*             JsonObject roomsJson = propertyInfo.getAsJsonObject("rooms");
            int numberOfRooms = roomsJson.get("raw").getAsInt(); */

            int ageYears = Year.now().getValue() - propertyInfo.get("constructionYear").getAsInt();

            JsonObject operatingCostJson = propertyInfo.getAsJsonObject("operatingCost");
            int maintenanceCostPerMonth = operatingCostJson.get("raw").getAsInt();


            switch (resedentialType) {
                case "Villa":

                    int additionalArea = 0;
                    if(propertyInfo.get("additionalArea").isJsonObject()){
                        JsonObject additionalAreaJson = propertyInfo.getAsJsonObject("additionalArea");

                        additionalArea = additionalAreaJson.get("raw").getAsInt();
                    }

                    int plotArea = 0;
                    if(propertyInfo.get("plotArea").isJsonObject()){
                        JsonObject plotAreaJson = propertyInfo.getAsJsonObject("plotArea");

                        plotArea = plotAreaJson.get("raw").getAsInt();
                    }

                    System.err.println("\n");
                    System.out.println("x: " + latitude);
                    System.out.println("y: " + longitude);
                    System.out.println("Pris: " + price + "kr");
                    System.out.println("Boarea: " + livingAreaMetresSquared + "kvm");
                    /* System.out.println("Antal rum: " + numberOfRooms); */
                    System.out.println("Ålder: " + ageYears + "år");
                    System.out.println("Driftkostnad: " + maintenanceCostPerMonth + "kr per månad");
                    System.out.println("Biarea: " + additionalArea + " kvm");
                    System.out.println("Tomtarea: " + plotArea + " kvm");
                    System.err.println("\n");

                    break;
            
                default:
                    throw new InvalidArgumentException("Invalid residential type");
            }


        } catch (Exception e) {
            if(e instanceof java.lang.ClassCastException || e instanceof java.lang.UnsupportedOperationException){
                System.err.println("ClassExeption: Missing Critical Information");
            }else{
                System.err.println(e);
            }

        }
    }
}
