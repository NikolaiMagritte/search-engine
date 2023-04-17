package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.BadRequest;
import searchengine.dto.indexing.OkResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;
import searchengine.utils.ErrorsAndLogsUtil;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
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
            return new ResponseEntity<>(new BadRequest(false, ErrorsAndLogsUtil.ERROR_INDEXING_ALREADY_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        if (indexingService.stopIndexing()) {
            return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new BadRequest(false, ErrorsAndLogsUtil.ERROR_INDEXING_NOT_STARTED),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage (@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            return new ResponseEntity<>(new BadRequest(false, ErrorsAndLogsUtil.ERROR_NOT_AVAIBLE_PAGE),
                    HttpStatus.BAD_REQUEST);
        } else {
            if (indexingService.startPageIndexing(url)) {
                return new ResponseEntity<>(new OkResponse(true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new BadRequest(false, ErrorsAndLogsUtil.ERROR_NOT_AVAIBLE_PAGE),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "")
                                             String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "")
                                             String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0")
                                             int offset,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20")
                                             int limit) {

        return searchService.search(query, site, offset, limit);
    }

}
