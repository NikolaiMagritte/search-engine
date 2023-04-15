package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SearchResponse {
    private boolean result;
    private int count;
    List<SearchData> data;
}
