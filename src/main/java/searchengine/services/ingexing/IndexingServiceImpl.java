package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingRequest;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Component
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final JsoupConfig jsoupConfig;


    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing()) {
            return new IndexingResponse(false, "Индексация уже запущена");
        }

        deleteSites();

        for (searchengine.config.Site site : sitesList.getSites()) {
            String url = site.getUrl();

            siteRepository.save(Site.builder()
                    .name(site.getName())
                    .status(SiteStatus.INDEXING)
                    .url(url.toLowerCase())
                    .statusTime(LocalDateTime.now())
                    .build());
        }

        for (Site site : siteRepository.findAll()) {
            new UrlParser(site, "/", pageRepository, siteRepository, indexRepository, lemmaRepository, jsoupConfig, true).fork();
        }

        return new IndexingResponse(true, null);
    }

    private void deleteSites() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
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

    @Override
    public IndexingResponse indexPage(IndexingRequest indexingRequest) {
        String siteUrl = "";
        String path = "/";
        try {
            URL url = new URL(indexingRequest.url());
            siteUrl = url.getProtocol() + "://" + url.getHost();
            path = url.getPath();
        } catch (MalformedURLException ignore) {
        }

        path = path.trim();
        path = path.isBlank() ? "/" : path;

        Optional<Site> optional = siteRepository.findByUrlIgnoreCase(siteUrl);

        if (optional.isPresent()) {
            Site site = optional.get();
            if (!site.getStatus().equals(SiteStatus.INDEXED)) {
                return new IndexingResponse(false, "Сайт не прошёл индексацию");
            }
            setIndexingStatus(site);
            deletePage(site, path);
            new UrlParser(site, path, pageRepository, siteRepository, indexRepository, lemmaRepository, jsoupConfig, true).fork();
            return new IndexingResponse(true, null);
        } else {
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
    }

    private void setIndexingStatus(Site site) {
        site.setStatus(SiteStatus.INDEXING);
        siteRepository.save(site);
    }

    private void deletePage(Site site, String path) {
        Optional<Page> optional = pageRepository.findBySiteAndPath(site, path);
        optional.ifPresent(pageRepository::delete);
    }


    private boolean isIndexing() {
        return siteRepository.existsByStatus(SiteStatus.INDEXING);
    }
}