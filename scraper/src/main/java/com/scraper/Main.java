package com.scraper;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {
    static String[] residentialTypes = {
            "Villa",
            "Lägenhet",
            "Kedjehus-Parhus-Radhus",
            "Fritidshus",
            "Gård",
            "Tomt/Mark"
    };

    private static void dbWrite() {
        // TODO: DBWRITE FUNCTION
    }

    private static void scrapePage(String url, WebDriver driver) {
        driver.get(url);
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(residentialTypes.length);

        for (String residentialType : residentialTypes) {
            final String type = residentialType;
            executor.submit(() -> {
                WebDriver driver = new ChromeDriver();
                for (int i = 1; i <= 1000; i++) {
                    driver.get("https://www.booli.se/sok/slutpriser?objectType=" + type + "&page=" + i);

                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(50));

                    List<WebElement> hyperLinks = driver.findElements(By.cssSelector("[href^='/annons/'], [href^='/bostad/']"));

                    List<String> hrefs = new ArrayList<>();
                    for(WebElement link : hyperLinks){
                        hrefs.add(link.getAttribute("href"));
                    }

                    for (String href : hrefs) {
                        scrapePage(href, driver);
                    }

                    System.out.println(type + " progress: " + i / 10 + "%");
                }
                System.out.println(type + " finished!");

                driver.close();
            });
        }
    };
}