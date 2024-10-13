package example.dividends_project.scraper;

import example.dividends_project.model.Company;
import example.dividends_project.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
