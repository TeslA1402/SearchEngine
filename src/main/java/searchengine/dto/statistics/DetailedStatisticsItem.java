package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetailedStatisticsItem(String url, String name, String status, long statusTime, String error, int pages,
                                     int lemmas) {
}
