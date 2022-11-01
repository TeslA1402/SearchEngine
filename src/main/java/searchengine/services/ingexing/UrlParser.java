package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import searchengine.config.JsoupConfig;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
public class UrlParser extends RecursiveAction {
    private final Site site;
    private final String path;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final JsoupConfig jsoupConfig;
    private final boolean isFirstAction;
    private static final Random random = new Random();

    @Override
    protected void compute() {
        if (isNotFailed()) {
            try {
                Thread.sleep(jsoupConfig.getTimeoutMin() + Math.abs(random.nextInt()) % jsoupConfig.getTimeoutMax() - jsoupConfig.getTimeoutMin());

                Connection.Response response = getResponse();

                Document document = response.parse();

                savePage(response.statusCode(), document.html());

                Set<String> paths = getPaths(document);

                paths.forEach(path -> {
                    if (!isVisited(path)) {
                        new UrlParser(site, path, pageRepository, siteRepository, jsoupConfig, false).invoke();
                    }
                });

                updateStatusTime();

                if (isFirstAction && isNotFailed()) {
                    indexed();
                }
            } catch (UnsupportedMimeTypeException ignored) {
            } catch (Exception ignored) {
                failed();
            }
        }
    }

    private void failed() {
        site.setLastError("Ошибка парсинга URL: " + site.getUrl() + path);
        site.setStatus(SiteStatus.FAILED);
        siteRepository.save(site);
    }

    private Connection.Response getResponse() throws IOException {
        return Jsoup.connect(site.getUrl() + path)
                .maxBodySize(0)
                .userAgent(jsoupConfig.getUserAgent())
                .referrer(jsoupConfig.getReferrer())
                .ignoreHttpErrors(true)
                .execute();
    }

    private void savePage(int code, String content) {
        pageRepository.save(Page.builder()
                .path(path)
                .site(site)
                .code(code)
                .content(content)
                .build());
    }

    private void updateStatusTime() {
        Site persistSite = getPersistSite();
        persistSite.setStatusTime(LocalDateTime.now());
        siteRepository.save(persistSite);
    }

    private void indexed() {
        Site persistSite = getPersistSite();
        persistSite.setStatus(SiteStatus.INDEXED);
        siteRepository.save(persistSite);
    }

    private Site getPersistSite() {
        return siteRepository.findById(site.getId()).orElseThrow(() -> new IllegalStateException("Site not found"));
    }

    private boolean isVisited(String path) {
        return pageRepository.existsBySiteAndPath(site, path);
    }

    private boolean isNotFailed() {
        return !siteRepository.existsByIdAndStatus(site.getId(), SiteStatus.FAILED);
    }

    private Set<String> getPaths(Document document) {
        return document.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .filter(path -> path.startsWith("/"))
                .collect(Collectors.toSet());
    }
}
