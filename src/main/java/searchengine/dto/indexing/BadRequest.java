package searchengine.dto.indexing;

import lombok.Value;

@Value
public class BadRequest {
    boolean result;
    String error;
}
