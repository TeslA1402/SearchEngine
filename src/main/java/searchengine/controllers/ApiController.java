package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingRequest;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.ingexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    @ResponseStatus(HttpStatus.OK)
    public StatisticsResponse statistics() {
        return statisticsService.getStatistics();
    }

    @GetMapping("/startIndexing")
    @ResponseStatus(HttpStatus.OK)
    public IndexingResponse startIndexing() {
        return indexingService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    @ResponseStatus(HttpStatus.OK)
    public IndexingResponse stopIndexing() {
        return indexingService.stopIndexing();
    }

    @PostMapping(value = "/indexPage", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public IndexingResponse indexPage(@Valid IndexingRequest indexingRequest) {
        return indexingService.indexPage(indexingRequest);
    }

    @GetMapping(value = "/search")
    @ResponseStatus(HttpStatus.OK)
    public SearchResponse search(@RequestParam String query, @RequestParam(required = false) String site,
                                 @RequestParam(required = false, defaultValue = "0") Integer offset,
                                 @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return searchService.search(query, site, offset, limit);
    }
}
