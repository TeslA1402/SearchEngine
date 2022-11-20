package searchengine.dto.indexing;

import javax.validation.constraints.NotNull;

public record IndexingRequest(@NotNull String url) {
}
