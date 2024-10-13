package example.dividends_project;

import example.dividends_project.model.Company;
import example.dividends_project.scraper.YahooFinanceScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DividendsProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DividendsProjectApplication.class, args);

//        System.out.println("Main -> "  + Thread.currentThread().getName());
    }
}
