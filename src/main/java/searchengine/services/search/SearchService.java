package searchengine.services.search;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;

import java.util.List;

public interface SearchService {
    ResponseEntity<Object> search(String query, String url, int offset, int limit);
    List<SearchData> searchThroughAllSites(String query, int offset, int limit);
    List<SearchData> onePageSearch(String query, String url, int offset, int limit);

}
