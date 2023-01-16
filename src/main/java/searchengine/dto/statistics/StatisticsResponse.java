package searchengine.dto.statistics;

public record StatisticsResponse(boolean result, StatisticsData statistics) {
    public StatisticsResponse(StatisticsData statistics) {
        this(true, statistics);
    }
}
