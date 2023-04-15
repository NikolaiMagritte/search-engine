package searchengine.services.indexing;

public interface IndexingService {
    boolean startIndexing();
    boolean startPageIndexing(String page);
    boolean stopIndexing();
}
