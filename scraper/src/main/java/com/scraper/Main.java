package com.scraper;
import java.io.IOException;

import org.jsoup.*; 
import org.jsoup.nodes.*; 
import org.jsoup.select.*;


public class Main {
    private static int progress = 1;


    private static void scrapePage(String href){
        String url = "https://www.booli.se" + href;

        try {
            Document annonsPage = Jsoup.connect(url).get();



        } catch (IOException error) {
            throw new RuntimeException(error);
        }

    }

     public static void main(String[] args) {
        try {
            while(true){             
                String url = "https://www.booli.se/sok/slutpriser?page=" + progress;

                Document slutpriserPage = Jsoup.connect(url).get();

                Elements links = slutpriserPage.select("[href^=/annons/]");

                linkFor: for(Element link : links){
                    Element article = link.closest("article");

                    Elements spanList = article.select("span");

                    for(int i = 0; i < spanList.size(); i++){
                        Element span = spanList.get(i);

                        if(span.text().startsWith("Lägenhet") || span.text().startsWith("Villa")){
                            break;
                        }
                        if(i == spanList.size() - 1){
                            break linkFor;
                        }
                    }

                    Attribute href = link.attribute("href");

                    scrapePage(href.getValue());
                }

                progress++;
            }

        }catch(HttpStatusException error){
            if(error.getStatusCode() == 404){
                System.out.println("End of countent found on page " + progress);
            }
        }catch(IOException error){
            throw new RuntimeException(error);
        }
    }
}