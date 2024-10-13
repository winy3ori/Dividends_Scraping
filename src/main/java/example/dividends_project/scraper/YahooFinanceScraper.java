package example.dividends_project.scraper;

import example.dividends_project.model.Company;
import example.dividends_project.model.Dividend;
import example.dividends_project.model.ScrapedResult;
import example.dividends_project.model.constants.Month;
import example.dividends_project.persist.entity.DividendEntity;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&frequency=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400; // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long end = System.currentTimeMillis() / 1000; // 현재 시간을 초 단위로 나눔

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, end);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByClass("table yf-ewueuo noDl");
            Element tableEle = parsingDivs.get(0);

            //table 구조상 thead, tbody, tfoot 순서. 여기서는 tbody를 가져오기 위해 get(1) 사용
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumver(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.selectFirst("h1.yf-xxbei9");

//          기존 코드 1   Element titleEle = document.getElementsByTag("h1").get(0);
//          기존 코드 2   String title = titleEle.text().split(" - ")[1].trim();
            System.out.println("titleEle  = " + titleEle);

            // 회사명에서 티커 부분을 제거하고 이름만 추출
            String title = titleEle.text().replaceAll("\\(.*\\)", "").trim();

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
