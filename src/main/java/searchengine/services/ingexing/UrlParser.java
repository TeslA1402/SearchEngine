package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import searchengine.config.JsoupConfig;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UrlParser extends RecursiveAction {
    private final Site site;
    private final String path;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final JsoupConfig jsoupConfig;
    private final boolean isFirstAction;
    private static final Random random = new Random();

    @Override
    protected void compute() {
        if (isNotFailed() && !isVisited()) {
            try {
                Thread.sleep(jsoupConfig.getTimeoutMin() + Math.abs(random.nextInt()) % jsoupConfig.getTimeoutMax() - jsoupConfig.getTimeoutMin());
                updateStatusTime();

                Connection.Response response = getResponse();
                Document document = response.parse();

                Optional<Page> optionalPage = savePage(response.statusCode(), document.html());

                if (optionalPage.isPresent()) {
                    findLemmas(optionalPage.get());

                    Set<ForkJoinTask<Void>> tasks = getPaths(document).stream().map(path -> new UrlParser(site, path, pageRepository, siteRepository, indexRepository, lemmaRepository, jsoupConfig, false).fork()).collect(Collectors.toSet());
                    tasks.forEach(ForkJoinTask::join);

                    if (isFirstAction && isNotFailed()) {
                        indexed();
                    }
                }
            } catch (UnsupportedMimeTypeException ignored) {
            } catch (Exception ignored) {
                failed("Ошибка парсинга URL: " + site.getUrl() + path);
            }
        }
    }

    private void findLemmas(Page page) {
        try {
            if (page.getCode() < 400) {
                new LemmaFinder(lemmaRepository, indexRepository).find(page);
            }
        } catch (Exception e) {
            failed("Ошибка индексации URL: " + site.getUrl() + path);
        }
    }

    private void failed(String error) {
        Site site = getPersistSite();
        site.setLastError(error);
        site.setStatus(SiteStatus.FAILED);
        siteRepository.save(site);
    }

    private Connection.Response getResponse() throws IOException {
        return Jsoup.connect(site.getUrl() + path)
                .maxBodySize(0)
                .userAgent(jsoupConfig.getUserAgent())
                .referrer(jsoupConfig.getReferrer())
                .header("Accept-Language", "ru")
                .ignoreHttpErrors(true)
                .execute();
    }

    private synchronized Optional<Page> savePage(int code, String content) {
        if (!isVisited()) {
            return Optional.of(pageRepository.save(Page.builder()
                    .path(path)
                    .site(site)
                    .code(code)
                    .content(content)
                    .build()));
        } else {
            return Optional.empty();
        }
    }

    private boolean isVisited() {
        return pageRepository.existsBySiteAndPath(site, path);
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
