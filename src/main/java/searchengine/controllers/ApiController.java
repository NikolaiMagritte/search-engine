package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.BadRequest;
import searchengine.dto.indexing.OkResponse;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final static String ERROR_INDEXING_ALREADY_STARTED = "Индексация уже запущена";
    private final static String ERROR_INDEXING_NOT_STARTED = "Индексация не запущена";
    private final static String ERROR_NOT_AVAIBLE_PAGE = "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SiteRepository siteRepository;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() {
        if (indexingService.startIndexing()) {
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequest(false, ERROR_INDEXING_ALREADY_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }
//    @GetMapping("/startIndexing")
//    public ResponseEntity<IndexingResponse> startIndexing() {
//        return ResponseEntity.ok(indexingService.startIndexing());
//    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        if (indexingService.stopIndexing()) {
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequest(false, ERROR_INDEXING_NOT_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }
//    @GetMapping("/stopIndexing")
//    public ResponseEntity<IndexingResponse> stopIndexing() {
//        return ResponseEntity.ok(indexingService.stopIndexing());
//    }

    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage (@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            return new ResponseEntity<>(new BadRequest(false, ERROR_NOT_AVAIBLE_PAGE), HttpStatus.BAD_REQUEST);
        } else {
            if (indexingService.startPageIndexing(url)) {
                return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new BadRequest(false, ERROR_NOT_AVAIBLE_PAGE),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

//    @PostMapping("/indexPage")
//    public ResponseEntity<IndexingResponse> indexPage (@RequestParam(name = "url") String url) {
//        return ResponseEntity.ok(indexingService.startPageIndexing(url));
//    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "")
                                             String request,
                                         @RequestParam(name = "site", required = false, defaultValue = "")
                                             String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0")
                                             int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20")
                                             int limit) {
        if (request.isEmpty()) {
            return new ResponseEntity<>(new IndexingResponse(false, "Задан пустой поисковый запрос"),
                    HttpStatus.BAD_REQUEST);
        } else {
            List<SearchData> searchData;
            if (!site.isEmpty()) {
                if (siteRepository.findByUrl(site) == null) {
                    return new ResponseEntity<>(new IndexingResponse(false, "Указанная страница не найдена"),
                            HttpStatus.BAD_REQUEST);
                } else {
                    searchData = searchService.onePageSearch(request, site, offset, limit);
                }
            } else {
                searchData = searchService.searchThroughAllSites(request, offset, limit);
            }
            if (searchData == null) {
                return new ResponseEntity<>(new IndexingResponse(false, "По данному запросу ни чего не найдено."),
                        HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);
        }
    }

}
