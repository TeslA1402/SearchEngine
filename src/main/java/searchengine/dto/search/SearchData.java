package searchengine.dto.search;

public record SearchData(String site, String siteName, String uri, String title, String snippet, float relevance) {
}
