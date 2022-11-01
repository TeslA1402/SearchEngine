package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Component
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final JsoupConfig jsoupConfig;


    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        sitesList.getSites().forEach(site -> {
                    String url = site.getUrl();
                    pageRepository.deleteBySiteUrlIgnoreCase(url);
                    siteRepository.deleteByUrlIgnoreCase(url);
                }
        );

        sitesList.getSites().forEach(site -> siteRepository.save(Site.builder()
                .name(site.getName())
                .status(SiteStatus.INDEXING)
                .url(site.getUrl().toLowerCase())
                .statusTime(LocalDateTime.now())
                .build()));

        siteRepository.findAll().forEach(site -> new UrlParser(site, "/", pageRepository, siteRepository, jsoupConfig, true).fork());
        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing()) {
            return new IndexingResponse(false, "Индексация не запущена");
        }

        siteRepository.findAllByStatus(SiteStatus.INDEXING).forEach(site -> {
            site.setLastError("Индексация остановлена пользователем");
            site.setStatus(SiteStatus.FAILED);
            siteRepository.save(site);
        });

        return new IndexingResponse(true, null);
    }

    private boolean isIndexing() {
        return siteRepository.existsByStatus(SiteStatus.INDEXING);
    }
}