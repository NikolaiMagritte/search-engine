package searchengine.services.search;

import searchengine.dto.search.SearchData;

import java.util.List;

public interface SearchService {
    List<SearchData> searchThroughAllSites(String query, int offset, int limit);
    List<SearchData> onePageSearch(String query, String url, int offset, int limit);

}
