package searchengine.dto.search;

import java.util.List;

public record SearchResponse(boolean result, long count, List<SearchData> data) {
    public SearchResponse(long count, List<SearchData> data) {
        this(true, count, data);
    }
}
