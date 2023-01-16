package searchengine.dto.indexing;

public record IndexingResponse(boolean result) {
    public IndexingResponse() {
        this(true);
    }
}
