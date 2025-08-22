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
 * Fix mpn issue,
 * Add tag support,
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

    private static String getId(WebDriver driver){
        List<WebElement> applicationScripts  = driver.findElements(By.cssSelector("script[type=\"application/ld+json\"]"));

            for(WebElement script : applicationScripts){
                String content = script.getAttribute("textContent");
                    
                if(!content.startsWith("{\"@context\":\"https://schema.org\",\"@type\":\"Product\",\"name\"")){
                    continue;
                }

                JsonObject contentJson = JsonParser.parseString(content).getAsJsonObject();

                String mpn = contentJson.get("mpn").toString();

                return mpn.replace("\"", "");
            }

        throw new IllegalArgumentException("Could not find id, no mpn");
    }

    private static void scrapePage(String url, WebDriver driver, String residentialType) {

        try {
             driver.get(url);

            String id = getId(driver);

            WebElement NEXT_DATA = driver.findElement(By.id("__NEXT_DATA__"));

            String NEXT_DATAContent = NEXT_DATA.getAttribute("textContent");

            JsonObject object = JsonParser.parseString(NEXT_DATAContent).getAsJsonObject();

            JsonObject props = object.getAsJsonObject("props");

            JsonObject pageProps = props.getAsJsonObject("pageProps");

            JsonObject APOLLO_STATE = pageProps.getAsJsonObject("__APOLLO_STATE__");

            JsonObject propertyInfo = APOLLO_STATE.getAsJsonObject("SoldProperty:".concat(id));

            DatabaseInteraction.Write(residentialType, propertyInfo);

        } catch (Exception e) {
            if(e instanceof java.lang.IllegalArgumentException){
                System.err.println(e.getMessage());
            }else{
                System.err.println(e);
            }
        }

    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(residentialTypes.length);

        for (String residentialType : residentialTypes) {
            final String type = residentialType;
            executor.submit(() -> {
                WebDriver driver = new ChromeDriver();
                for (int i = 1; i <= 1000; i++) {
                    System.out.println("Initialising scraping of page number: " + i + "/1000 (" + type + ")");

                    driver.get("https://www.booli.se/sok/slutpriser?objectType=" + type + "&page=" + i);

                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50));

                    List<WebElement> hyperLinks = driver.findElements(By.cssSelector("[href^='/annons/'], [href^='/bostad/']"));

                    List<String> hrefs = new ArrayList<>();
                    for(WebElement link : hyperLinks){
                        hrefs.add(link.getAttribute("href"));
                    }

                    for (String href : hrefs) {
                        scrapePage(href, driver, type);
                    }

                }
                System.out.println(type + " finished!");

                driver.close();
            });
        }
    };
}