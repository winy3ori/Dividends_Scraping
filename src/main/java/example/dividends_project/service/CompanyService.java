package example.dividends_project.service;

import example.dividends_project.exception.impl.NoCompanyException;
import example.dividends_project.model.Company;
import example.dividends_project.model.ScrapedResult;
import example.dividends_project.persist.entity.CompanyEntity;
import example.dividends_project.persist.entity.DividendEntity;
import example.dividends_project.persist.repository.CompanyRepository;
import example.dividends_project.persist.repository.DividendRepository;
import example.dividends_project.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanaceScrapper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker){
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);

        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker){
        // ticker 을 기준으로 회사를 스크래핑
        Company company = this.yahooFinanaceScrapper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 회사가 존재할 경우 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanaceScrapper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapedResult.getDividends().stream()
                                                        .map(e -> new DividendEntity(companyEntity.getId(), e))
                                                        .collect(Collectors.toList());
        this.dividendRepository.saveAll(dividendEntityList);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }


    public void addAutoCompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autoComplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                // .limit(10) 서비스에 맞게 가져올 개수 제한
                .collect(Collectors.toList());
    }

    public void deleteAutoCompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String  deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker).orElseThrow(()-> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutoCompleteKeyword(company.getName());

        return company.getName();
    }
}
