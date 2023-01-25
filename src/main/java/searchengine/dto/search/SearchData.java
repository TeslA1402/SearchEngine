package searchengine.dto.search;

import searchengine.model.Page;

public record SearchData(String site, String siteName, String uri, String title, String snippet, float relevance) {
    public SearchData(Page page, String title, String snippet, float relevance) {
        this(page.getSite().getUrl(), page.getSite().getName(), page.getPath(), title, snippet, relevance);
    }
}
