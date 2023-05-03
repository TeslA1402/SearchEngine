package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingRequest;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exception.BadRequestException;
import searchengine.exception.NotFoundException;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.lemma.LemmaService;
import searchengine.utils.HtmlParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final LemmaService lemmaService;
    private final HtmlParser htmlParser;


    @Override
    public IndexingResponse startIndexing() {
        log.info("Start indexing");
        if (existsIndexingSite()) {
            log.warn("Indexing already start");
            throw new BadRequestException("Индексация уже запущена");
        }

        deleteSites();

        for (searchengine.config.Site site : sitesList.getSites()) {
            String url = site.getUrl();
            log.info("Save site with url: {}", url);
            siteRepository.save(Site.builder()
                    .name(site.getName())
                    .status(SiteStatus.INDEXING)
                    .url(url.toLowerCase())
                    .statusTime(LocalDateTime.now())
                    .build());
        }

        for (Site site : siteRepository.findAll()) {
            log.info("Start indexing site: {}", site);
            runParser(site.getId(), "/");
        }
        return new IndexingResponse();
    }

    private void deleteSites() {
        log.info("Delete all sites");
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
    }

    @Override
    public IndexingResponse stopIndexing() {
        log.info("Stop indexing");
        if (!existsIndexingSite()) {
            log.warn("Indexing not run");
            throw new BadRequestException("Индексация не запущена");
        }

        siteRepository.findAllByStatus(SiteStatus.INDEXING).forEach(site -> {
            site.setLastError("Индексация остановлена пользователем");
            site.setStatus(SiteStatus.FAILED);
            siteRepository.save(site);
        });

        return new IndexingResponse();
    }

    @Override
    public IndexingResponse indexPage(IndexingRequest indexingRequest) {
        String requestUrl = indexingRequest.url();
        log.info("Index page: {}", requestUrl);
        String siteUrl = "";
        String path = "/";
        try {
            URL url = new URL(requestUrl);
            siteUrl = url.getProtocol() + "://" + url.getHost();
            path = url.getPath();
        } catch (MalformedURLException e) {
            log.error("URL parser error", e);
        }

        path = path.trim();
        path = path.isBlank() ? "/" : path;

        Optional<Site> optional = siteRepository.findByUrlIgnoreCase(siteUrl);

        if (optional.isPresent()) {
            Site site = optional.get();
            if (!site.getStatus().equals(SiteStatus.INDEXED)) {
                log.warn("Site in not INDEXED status");
                throw new BadRequestException("Сайт не прошёл индексацию");
            }
            indexing(site.getId());
            deletePage(site, path);
            runParser(site.getId(), path);
            return new IndexingResponse();
        } else {
            log.warn("Site not found: {}", siteUrl);
            throw new NotFoundException("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        }
    }

    private void runParser(Integer siteId, String path) {
        new UrlParser(siteId, path,
                siteRepository, pageRepository,
                lemmaService,
                htmlParser,
                true).fork();
    }

    private void deletePage(Site site, String path) {
        log.info("Delete page {} for site {}", path, site);
        Optional<Page> optional = pageRepository.findBySiteAndPath(site, path);
        optional.ifPresent(pageRepository::delete);
    }

    private void indexing(Integer siteId) {
        Site site = siteRepository.findById(siteId).orElseThrow(() -> new IllegalStateException("Site not found"));
        site.setStatus(SiteStatus.INDEXING);
        siteRepository.save(site);
        log.info("Site indexing: {}", site);
    }

    private boolean existsIndexingSite() {
        return siteRepository.existsByStatus(SiteStatus.INDEXING);
    }
}