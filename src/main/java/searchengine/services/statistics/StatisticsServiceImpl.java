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
            DetailedStatisticsItem detailedStatisticsItem = new DetailedStatisticsItem(site.getUrl(), site.getName(),
                    site.getStatus().name(),
                    site.getStatusTime().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli(),
                    site.getLastError(), (int) pageRepository.countBySite(site),
                    (int) lemmaRepository.countBySite(site));
            detailed.add(detailedStatisticsItem);
        }

        TotalStatistics total = new TotalStatistics((int) siteRepository.count(),
                (int) pageRepository.count(),
                (int) lemmaRepository.count(),
                !siteRepository.existsByStatusNot(SiteStatus.INDEXED));

        StatisticsData statistics = new StatisticsData(total, detailed);

        return new StatisticsResponse(statistics);
    }
}
