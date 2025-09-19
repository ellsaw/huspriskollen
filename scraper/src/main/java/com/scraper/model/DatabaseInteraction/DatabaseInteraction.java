package com.scraper.model.DatabaseInteraction;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DatabaseInteraction {
    private JsonObject propertyInfo;
    private String resedentialType;

    public DatabaseInteraction(String resedentialType, JsonObject propertyInfo){
        this.propertyInfo = propertyInfo;
        this.resedentialType = resedentialType;
    }
    
    private static int getUnix(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        return (int) localDateTime.toEpochSecond(ZoneOffset.UTC);
    }

    public void write() {
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

            int hasFireplace = 0;
            int hasBalcony = 0;
            int hasPatio = 0;
            int hasElevator = 0;

            if(propertyInfo.has("amenities")){
                Vector<String> ameneties = new Vector<String>();

                JsonArray amenityArray = propertyInfo.getAsJsonArray("amenities");

                for(JsonElement ref : amenityArray){
                    JsonObject refObject = ref.getAsJsonObject();

                    String amenityString = refObject.getAsJsonPrimitive("__ref").getAsString();

                    int colonIndex = amenityString.indexOf(":");

                    String value = amenityString.substring(colonIndex + 1);

                    JsonObject amenityObject = JsonParser.parseString(value).getAsJsonObject();

                    String amenity = amenityObject.get("key").getAsString();

                    ameneties.add(amenity);
                };

                for(String amenity : ameneties){
                    switch (amenity) {
                        case "fireplace":
                            hasFireplace = 1;
                            break;
                        case "balcony":
                            hasBalcony = 1;
                            break;
                        case "patio":
                            hasPatio = 1;
                            break;
                        case "elevator":
                            hasElevator = 1;
                            break;
                        default:
                            break;
                    }
                }

            }
            

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

                    sqlGateway.writeVilla(latitude, longitude, price, livingAreaMetresSquared, ageYears, maintenanceCostPerMonth, additionalAreaMetresSquared, plotAreaMetresSquared, dateUnix, hasFireplace, hasBalcony, hasPatio);

                    break;
            
                default:
                    throw new IllegalArgumentException("Invalid residential type");
            }


        } catch (Exception e) {
            if(e instanceof java.lang.ClassCastException || e instanceof java.lang.UnsupportedOperationException){
                System.err.println("Missing critical information, discarding");
            }else{
                System.err.println(e);
            }

        }
    }
}
