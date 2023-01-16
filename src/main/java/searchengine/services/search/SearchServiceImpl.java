package searchengine.services.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.exception.BadRequestException;

import java.util.List;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {
    @Override
    public SearchResponse search(String query, String site, Long offset, Long limit) {
        if (query == null || query.isBlank()) {
            throw new BadRequestException("Задан пустой поисковый запрос");
        }
        return new SearchResponse(1, List.of(new SearchData("https://google.com", "Google", "/", "Главная", "kek", 0.99f)));
    }
}
