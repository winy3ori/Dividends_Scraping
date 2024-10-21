package example.dividends_project.scheduler;

import example.dividends_project.model.Company;
import example.dividends_project.model.ScrapedResult;
import example.dividends_project.model.constants.CacheKey;
import example.dividends_project.persist.entity.CompanyEntity;
import example.dividends_project.persist.entity.DividendEntity;
import example.dividends_project.persist.repository.CompanyRepository;
import example.dividends_project.persist.repository.DividendRepository;
import example.dividends_project.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j  // 로깅
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yaooFinanceScraper;

//    @Scheduled(fixedDelay = 1000)
    public void test1() throws InterruptedException {
        Thread.sleep(10000);
        System.out.println(Thread.currentThread().getName() + " -> 테스트1 : " + LocalDateTime.now());
    }

//    @Scheduled(fixedDelay = 1000)
    public void test2() {
        System.out.println(Thread.currentThread().getName() + " -> 테스트2 : " + LocalDateTime.now());
    }


    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");

        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yaooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));

            scrapedResult.getDividends().stream()
                    // Dividen 모델을 엔티티로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 하나씩 Dividen 레파지토리에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

        // 스크래핑한 대방금 정보 중 데이터베이스에 없는 값은 저장

    }
}
