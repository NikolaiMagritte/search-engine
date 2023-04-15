package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.utils.UrlUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final static String LOG_STOP_INDEXING = "-> Запускаем остановку индексации";
    private final static String LOG_START_INDEXING_PAGE = "-> Запущена индексация страницы: ";
    private final static String LOG_DONE_INDEXING_PAGE = "-> Завершена индексация страницы: ";
    private final static String ERROR_NOT_AVAIBLE_PAGE = "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";

    private ExecutorService executorService;

    private final IndexingOnePage indexingOnePage;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetDtoLemmas getDtoLemmas;
    private final GetDtoIndex getDtoIndex;
    private final SitesList sitesList;
    private final UrlUtil urlUtil;

    @Override
    public boolean startIndexing() {
        if (isIndexing()) {
            return false;
        } else {
            executorService = Executors.newFixedThreadPool(CORE_COUNT);
            for (Site site : sitesList.getSites()) {
                executorService.submit(new IndexingAllPages(siteRepository, pageRepository,
                        lemmaRepository, indexRepository, getDtoLemmas, getDtoIndex, site, urlUtil));
            }
            executorService.shutdown();
            return true;
        }
    }

    @Override
    public boolean startPageIndexing(String page) {
        if (isPageAvaible(page) && !page.isEmpty()) {
            log.info(LOG_START_INDEXING_PAGE + page);
            indexingOnePage.start(page);
            log.info(LOG_DONE_INDEXING_PAGE + page);
            return true;
        } else {
            log.info(ERROR_NOT_AVAIBLE_PAGE + page);
            return false;
        }
    }

    @Override
    public boolean stopIndexing() {
        if (isIndexing()) {
            log.info(LOG_STOP_INDEXING);
            executorService.shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    private boolean isIndexing() {
        return siteRepository.existsByStatus(SiteStatus.INDEXING);
    }

    private boolean isPageAvaible(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return true;
            }
        }
        return false;
    }

    public static int getCoreCount() {
        return CORE_COUNT;
    }

    @Scheduled(cron = "5 * * * * *")
    private void updateStatusTime() {
        List<SiteEntity> sites = siteRepository.findByStatus(SiteStatus.INDEXING);
        for (SiteEntity siteEntity : sites) {
            siteEntity.setStatusTime(new Date());

            siteRepository.save(siteEntity);

        }
    }
}