package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        log.info("Get statistics");
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : siteRepository.findAll()) {
            DetailedStatisticsItem detailedStatisticsItem = new DetailedStatisticsItem();
            detailedStatisticsItem.setStatus(site.getStatus().name());
            detailedStatisticsItem.setUrl(site.getUrl());
            detailedStatisticsItem.setName(site.getName());
            detailedStatisticsItem.setError(site.getLastError());
            detailedStatisticsItem.setStatusTime(site.getStatusTime().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
            detailedStatisticsItem.setLemmas(lemmaRepository.countBySite(site));
            detailedStatisticsItem.setPages(pageRepository.countBySite(site));
            detailed.add(detailedStatisticsItem);
        }

        TotalStatistics total = new TotalStatistics();
        total.setIndexing(!siteRepository.existsByStatusNot(SiteStatus.INDEXED));
        total.setSites((int) siteRepository.count());
        total.setLemmas((int) lemmaRepository.count());
        total.setPages((int) pageRepository.count());

        StatisticsData statistics = new StatisticsData();
        statistics.setDetailed(detailed);
        statistics.setTotal(total);

        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setResult(true);
        statisticsResponse.setStatistics(statistics);

        return statisticsResponse;
    }
}
