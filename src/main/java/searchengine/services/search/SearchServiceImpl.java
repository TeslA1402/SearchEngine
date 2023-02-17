package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.exception.BadRequestException;
import searchengine.exception.NotFoundException;
import searchengine.model.*;
import searchengine.services.lemma.LemmaParser;
import searchengine.services.lemma.LemmaService;
import searchengine.services.page.PageService;
import searchengine.services.site.SiteService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final SiteService siteService;
    private final LemmaParser lemmaParser;

    @Override
    public SearchResponse search(String query, String site, Integer offset, Integer limit) {
        log.info("Query: {}", query);
        long startTime = System.currentTimeMillis();
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Задан пустой поисковый запрос");
        }
        List<Site> sites = getSites(site);
        Set<String> queryLemmas = lemmaParser.parseToLemmaWithCount(query.trim()).keySet();

        Map<Page, Double> pageRank = new HashMap<>();

        for (Site persistSite : sites) {
            List<Lemma> sortedLemmas = queryLemmas.stream()
                    .map(lemma -> lemmaService.findBySiteAndLemma(persistSite, lemma))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(Lemma::getFrequency))
                    .toList();

            Set<Lemma> lemmaSet = new HashSet<>(sortedLemmas);

            Set<Page> pages = getPages(sortedLemmas);

            pageRank.putAll(pages.stream().collect(Collectors.toMap(Function.identity(), page -> sumRank(page, lemmaSet))));
        }

        Optional<Double> optionalMaxRank = pageRank.values().stream().max(Double::compareTo);
        List<SearchData> searchData;
        if (optionalMaxRank.isEmpty()) {
            searchData = List.of();
        } else {
            double maxRank = optionalMaxRank.get();
            searchData = pageRank.entrySet().stream()
                    .map(entry -> new SearchData(entry.getKey(), pageService.getTitle(entry.getKey()), pageService.generateSnippet(query, entry.getKey()), (float) (entry.getValue() / maxRank)))
                    .sorted((a, b) -> Float.compare(b.relevance(), a.relevance()))
                    .toList();
        }
        log.info("Search time: {} ms.", System.currentTimeMillis() - startTime);
        return new SearchResponse(searchData.size(), subList(searchData, offset, limit));
    }

    private List<SearchData> subList(List<SearchData> searchData, Integer offset, Integer limit) {
        int fromIndex = offset;
        int toIndex = fromIndex + limit;

        if (toIndex > searchData.size()) {
            toIndex = searchData.size();
        }
        if (fromIndex > toIndex) {
            return List.of();
        }

        return searchData.subList(fromIndex, toIndex);
    }

    private double sumRank(Page page, Set<Lemma> lemmas) {
        return page.getIndices().stream()
                .filter(index -> lemmas.contains(index.getLemma()))
                .mapToDouble(Index::getRank).sum();
    }

    private Set<Page> getPages(List<Lemma> sortedLemmas) {
        if (sortedLemmas.isEmpty()) return Set.of();
        Set<Page> pages = sortedLemmas.get(0).getIndices().stream().map(Index::getPage).collect(Collectors.toSet());
        for (int i = 1; i < sortedLemmas.size(); i++) {
            pages = sortedLemmas.get(i).getIndices().stream().map(Index::getPage).filter(pages::contains).collect(Collectors.toSet());
        }
        return pages;
    }

    private List<Site> getSites(String site) {
        List<Site> sites;
        if (site == null || site.isBlank()) {
            sites = siteService.findAll();
        } else {
            String trimSite = site.trim();
            if (trimSite.matches("^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\b$")) {
                Site persistSite = siteService.findByUrlIgnoreCase(trimSite).orElseThrow(() -> new NotFoundException("Сайт не найден"));
                sites = List.of(persistSite);
            } else {
                throw new BadRequestException("Некорректный адрес сайта");
            }
        }
        checkIndexed(sites);
        return sites;
    }

    private void checkIndexed(List<Site> sites) {
        boolean allSitesIndexed = sites.stream().allMatch(site -> site.getStatus().equals(SiteStatus.INDEXED));
        if (!allSitesIndexed) {
            throw new BadRequestException("Сайт не проиндексирован");
        }
    }
}
