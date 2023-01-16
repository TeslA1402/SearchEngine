package searchengine.dto.statistics;

import java.util.List;

public record StatisticsData(TotalStatistics total, List<DetailedStatisticsItem> detailed) {
}
