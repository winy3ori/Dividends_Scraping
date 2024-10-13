package example.dividends_project.service;

import example.dividends_project.model.Company;
import example.dividends_project.model.Dividend;
import example.dividends_project.model.ScrapedResult;
import example.dividends_project.model.constants.CacheKey;
import example.dividends_project.persist.entity.CompanyEntity;
import example.dividends_project.persist.entity.DividendEntity;
import example.dividends_project.persist.repository.CompanyRepository;
import example.dividends_project.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 요청이 자주 들어오는가?
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 최초 조회를 제외한 이후 조회시 로그가 뜨지 않아야 캐시에서 데이터를 가져오는 것
        log.info("search company -> " + companyName);


        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity companyEntity = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명"));

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(companyEntity.getId());

        // 3. 결과 조합 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(companyEntity.getTicker(), companyEntity.getName()), dividends);
    }

}
