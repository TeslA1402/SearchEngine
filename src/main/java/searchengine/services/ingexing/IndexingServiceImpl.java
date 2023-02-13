package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingRequest;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exception.BadRequestException;
import searchengine.exception.NotFoundException;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.services.index.IndexService;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Component
@Slf4j
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sitesList;
    private final SiteService siteService;
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final IndexService indexService;


    @Override
    public IndexingResponse startIndexing() {
        log.info("Start indexing");
        if (siteService.existsIndexingSite()) {
            log.warn("Indexing already start");
            throw new BadRequestException("Индексация уже запущена");
        }

        deleteSites();

        for (searchengine.config.Site site : sitesList.getSites()) {
            String url = site.getUrl();
            log.info("Save site with url: {}", url);
            siteService.save(site.getName(), url);
        }

        for (Site site : siteService.findAll()) {
            log.info("Start indexing site: {}", site);
            new UrlParser(site, "/", siteService, pageService, lemmaService, true).fork();
        }
        return new IndexingResponse();
    }

    private void deleteSites() {
        log.info("Delete all sites");
        indexService.deleteAll();
        lemmaService.deleteAll();
        pageService.deleteAll();
        siteService.deleteAll();
    }

    @Override
    public IndexingResponse stopIndexing() {
        log.info("Stop indexing");
        if (!siteService.existsIndexingSite()) {
            log.warn("Indexing not run");
            throw new BadRequestException("Индексация не запущена");
        }

        siteService.stopIndexingAllSites();

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

        Optional<Site> optional = siteService.findByUrlIgnoreCase(siteUrl);

        if (optional.isPresent()) {
            Site site = optional.get();
            if (!site.getStatus().equals(SiteStatus.INDEXED)) {
                log.warn("Site in not INDEXED status");
                throw new BadRequestException("Сайт не прошёл индексацию");
            }
            siteService.indexing(site.getId());
            pageService.deletePage(site, path);
            new UrlParser(site, path, siteService, pageService, lemmaService, true).fork();
            return new IndexingResponse();
        } else {
            log.warn("Site not found: {}", siteUrl);
            throw new NotFoundException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
    }
}