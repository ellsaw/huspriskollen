package com.scraper;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scraper.model.DatabaseInteraction.DatabaseInteraction;

/* TODO:
 * Add tag support,
 * Add support for more areas
 * Add support for other residential types
 */

public class Main {
    static String[] residentialTypes = {
            "Villa",
/*             "Lägenhet",
            "Kedjehus-Parhus-Radhus",
            "Fritidshus",
            "Gård",
            "Tomt/Mark" */
    };

    static String[] areaIds = {
        "424" // More ids will be added
    };

    private static JsonObject getPropertyInfo(JsonObject APOLLO_STATE, String adress){
        for(String key : APOLLO_STATE.keySet()){

            if(!key.startsWith("SoldProperty")){
                continue;
            };


            JsonObject subObject = APOLLO_STATE.getAsJsonObject(key);

            if(!subObject.has("streetAddress")){
                continue;
            };

            if(subObject.get("streetAddress").getAsString().equals(adress)){
                return subObject;
            }
        };

        throw new IllegalArgumentException("NEXT_DATA does not include property info");
    };

    private static String getAdress(WebDriver driver){
        List<WebElement> metaTags = driver.findElements(By.tagName("meta"));

        for(WebElement metaTag : metaTags){
            String name = metaTag.getAttribute("name");

            if(name.equals("og:title")){
                String content = metaTag.getAttribute("content");

                if(content != null){
                    return content.split(",")[0];
                }
            }
        }

        throw new RuntimeException("Cannot find adress on page");
    }

    private static void scrapePage(String url, WebDriver driver, String residentialType) {

        try {
             driver.get(url);

            WebElement NEXT_DATA = driver.findElement(By.id("__NEXT_DATA__"));

            String NEXT_DATAContent = NEXT_DATA.getAttribute("textContent");

            JsonObject object = JsonParser.parseString(NEXT_DATAContent).getAsJsonObject();

            JsonObject props = object.getAsJsonObject("props");

            JsonObject pageProps = props.getAsJsonObject("pageProps");

            JsonObject APOLLO_STATE = pageProps.getAsJsonObject("__APOLLO_STATE__");

            String adress = getAdress(driver);

            JsonObject propertyInfo = getPropertyInfo(APOLLO_STATE, adress);

            DatabaseInteraction.Write(residentialType, propertyInfo);

        } catch (Exception e) {
            if(e instanceof java.lang.IllegalArgumentException){
                System.err.println(e.getMessage());
            }else{
                System.err.println(e);
            }
        }

    }

    private static int getPageLimit(WebDriver driver){
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50));

        List<WebElement> potentialPageLimits = driver.findElements(By.cssSelector(".m-2"));

        for(WebElement potentialPageLimit: potentialPageLimits){
            String textContent = potentialPageLimit.getAttribute("textContent");

            if(textContent.startsWith("Visar sida 1 av")){
                String substring = textContent.substring(textContent.lastIndexOf(' ') + 1);

                return Integer.parseInt(substring);
            }
        }

        throw new RuntimeException("Could not find page limit");

    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(residentialTypes.length);

        for (final String areaId : areaIds){
            for (final String residentialType : residentialTypes) {
                executor.submit(() -> {
                    WebDriver driver = new ChromeDriver();

                    driver.get("https://www.booli.se/sok/slutpriser?areaIds=" + areaId + "&objectType=" + residentialType + "&page=1");

                    int pageLimit = getPageLimit(driver);
                    
                     for (int i = 1; i <= pageLimit; i++) {
                        System.out.println("Initialising scraping of page number: " + i + "/" + pageLimit + " for " + residentialType + " in area " + areaId);

                        if(i != 1){
                            driver.get("https://www.booli.se/sok/slutpriser?areaIds=" + areaId + "&objectType=" + residentialType + "&page=" + i);
                        }
    
                        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50));
    
                        List<WebElement> hyperLinks = driver.findElements(By.cssSelector("[href^='/annons/'], [href^='/bostad/']"));
    
                        List<String> hrefs = new ArrayList<>();
                        for(WebElement link : hyperLinks){
                            hrefs.add(link.getAttribute("href"));
                        }
    
                        for (String href : hrefs) {
                            scrapePage(href, driver, residentialType);
                        }

                        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50));
    
                    }
                    System.out.println(residentialType + " finished in area " + areaId);
    
                    driver.close();
                });
            }
        }
    };
}