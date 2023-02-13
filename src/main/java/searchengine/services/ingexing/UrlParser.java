package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.UnsupportedMimeTypeException;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class UrlParser extends RecursiveAction {
    private final transient Site site;
    private final String path;
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final boolean isFirstAction;

    @Override
    protected void compute() {
        if (siteService.isNotFailed(site.getId()) && pageService.isNotVisited(site, path)) {
            try {
                siteService.updateStatusTime(site.getId());
                Optional<Page> optionalPage = pageService.savePage(site, path);

                if (optionalPage.isPresent()) {
                    Page page = optionalPage.get();

                    if (page.getCode() < 400) {
                        lemmaService.findAndSave(page);
                    }

                    Set<ForkJoinTask<Void>> tasks = pageService.getPaths(page).stream().map(pathFromPage -> new UrlParser(site, pathFromPage, siteService, pageService, lemmaService, false).fork()).collect(Collectors.toSet());
                    tasks.forEach(ForkJoinTask::join);

                    if (isFirstAction && siteService.isNotFailed(site.getId())) {
                        lemmaService.updateLemmasFrequency(site);
                        siteService.indexed(site.getId());
                    }
                }
            } catch (UnsupportedMimeTypeException ignore) {
            } catch (Exception e) {
                log.error("Parser exception", e);
                siteService.failed(site.getId(), "Ошибка парсинга URL: " + site.getUrl() + path);
            }
        }
    }
}
