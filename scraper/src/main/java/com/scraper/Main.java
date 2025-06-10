package com.scraper;
import java.io.IOException;

import org.jsoup.*; 
import org.jsoup.nodes.*; 
import org.jsoup.select.*;


public class Main {
    static String[] residentialTypes = {
        "Villa",
        "Lägenhet",
        "Kedjehus-Parhus-Radhus",
        "Fritidshus",
        "Gård",
        "Tomt/Mark"
    };

    private static void dbWrite(){
        // TODO: DBWRITE FUNCTION
    }

    private static void scrapePage(String href, String residentialType){
        String url = "https://www.booli.se" + href;

        System.out.println(residentialType + ", " + url);
/*         try {
            Document annonsPage = Jsoup.connect(url).get();

        } catch (IOException error) {
            throw new RuntimeException(error);
        } */

    }

     public static void main(String[] args) {
        for(String residentialType : residentialTypes){
            try {
                for(int i = 1; i <= 1000; i++){             
                    String url = "https://www.booli.se/sok/slutpriser?objectType=" + residentialType + "&page=" + i;

                    Document slutpriserPage = Jsoup.connect(url).get();

                    Elements links = slutpriserPage.select("[href^=/annons/]");

                    for(Element link : links){
                        Attribute href = link.attribute("href");

                        scrapePage(href.getValue(), residentialType);
                    }
                }
            }catch(IOException error){
                throw new RuntimeException(error);
            }
        };
    };
}