package com.scraper;
import java.io.IOException;

import org.jsoup.*; 
import org.jsoup.nodes.*; 
import org.jsoup.select.*;

public class Main {
    private static int progress = 1;
     public static void main(String[] args) {
        try {
            while(true){             
                String url = "https://www.booli.se/sok/slutpriser?page=" + progress;

                Document doc = Jsoup.connect(url).get();

                System.out.println("Content found on page " + progress);

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