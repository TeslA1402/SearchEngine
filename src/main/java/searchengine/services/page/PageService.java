package searchengine.services.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.JsoupConfig;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.services.lemma.LemmaParser;
import searchengine.services.lemma.WordLemmas;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {
    private final JsoupConfig jsoupConfig;
    private static final Random random = new Random();
    private final PageRepository pageRepository;
    private final LemmaParser lemmaParser;
    private static final int SYMBOLS_IN_SNIPPET = 180;


    @Transactional
    public Optional<Page> savePage(Site site, String path) throws IOException, InterruptedException {
        synchronized (pageRepository) {
            Connection.Response response = getResponse(site.getUrl() + path);
            if (isNotVisited(site, path)) {
                return Optional.of(pageRepository.save(Page.builder()
                        .path(path)
                        .site(site)
                        .code(response.statusCode())
                        .content(response.parse().html())
                        .build()));
            } else {
                return Optional.empty();
            }
        }
    }

    public boolean isNotVisited(Site site, String path) {
        return !pageRepository.existsBySiteAndPath(site, path);
    }

    public Set<String> getPaths(Page page) {
        Document document = Jsoup.parse(page.getContent());
        return document.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .filter(path -> path.startsWith("/"))
                .collect(Collectors.toSet());
    }


    private Connection.Response getResponse(String url) throws IOException, InterruptedException {
        Thread.sleep(jsoupConfig.getTimeoutMin() + Math.abs(random.nextInt()) % jsoupConfig.getTimeoutMax() - jsoupConfig.getTimeoutMin());

        return Jsoup.connect(url)
                .maxBodySize(0)
                .userAgent(jsoupConfig.getUserAgent())
                .referrer(jsoupConfig.getReferrer())
                .header("Accept-Language", "ru")
                .ignoreHttpErrors(true)
                .execute();
    }

    @Transactional
    public void deleteAll() {
        pageRepository.deleteAllInBatch();
    }

    @Transactional
    public void deletePage(Site site, String path) {
        log.info("Delete page {} for site {}", path, site);
        Optional<Page> optional = pageRepository.findBySiteAndPath(site, path);
        optional.ifPresent(pageRepository::delete);
    }

    public String htmlToText(Page page) {
        return Jsoup.parse(page.getContent()).text();
    }

    public String getTitle(Page page) {
        Document document = Jsoup.parse(page.getContent());
        return document.title();
    }

    public String generateSnippet(String query, Page page) {
        List<WordLemmas> queryLemmas = lemmaParser.parseToWordWithLemmas(query);
        List<WordLemmas> wordLemmas = lemmaParser.parseToWordWithLemmas(htmlToText(page));
        List<WordLemmas> pageCommonSequence = commonSequence(queryLemmas, wordLemmas);
        return paddingSnippet(page, pageCommonSequence);
    }

    private String paddingSnippet(Page page, List<WordLemmas> pageCommonSequence) {
        String[] split = htmlToText(page).trim().split("\\s+");
        int startIndex = pageCommonSequence.get(0).getIndex();
        int endIndex = pageCommonSequence.get(pageCommonSequence.size() - 1).getIndex() + 1;
        int length = 0;
        int leftIndex = startIndex;
        int rightIndex = endIndex;
        while (length < SYMBOLS_IN_SNIPPET) {
            if (leftIndex > 0) {
                leftIndex--;
                length += split[leftIndex].length() + 1;
            }
            if (rightIndex < split.length) {
                length += split[rightIndex].length() + 1;
                rightIndex++;
            }
            if (startIndex == 0 && endIndex == split.length) {
                break;
            }
        }
        return (String.join(" ", Arrays.asList(split).subList(leftIndex, startIndex)) + " <b>" + String.join(" ", Arrays.asList(split).subList(startIndex, endIndex)) + "</b> " + String.join(" ", Arrays.asList(split).subList(endIndex, rightIndex))).trim();
    }

    private List<WordLemmas> commonSequence(List<WordLemmas> query, List<WordLemmas> page) {
        List<List<WordLemmas>> buffer = new ArrayList<>();
        for (int i = 0; i < page.size(); i++) {
            for (int j = 0; j < query.size(); j++) {
                if (page.get(i).equals(query.get(j))) {
                    List<WordLemmas> list = new ArrayList<>();
                    int k = 0;
                    while (i + k < page.size() && j + k < query.size() && page.get(i + k).equals(query.get(j + k))) {
                        list.add(page.get(i + k));
                        k++;
                    }
                    buffer.add(list);
                }
            }
        }
        return buffer.stream().max(Comparator.comparingInt(List::size)).orElseThrow(() -> new IllegalStateException("Common sequence not found"));
    }
}
