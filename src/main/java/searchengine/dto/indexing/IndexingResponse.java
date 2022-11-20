package searchengine.dto.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IndexingResponse(boolean result, String error) {
}
