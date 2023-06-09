package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.dto.indexing.DtoIndex;
import searchengine.dto.indexing.DtoLemma;
import searchengine.dto.indexing.DtoPage;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.IndexingServiceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public class IndexingAllPagesUtil implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetDtoLemmasUtil getDtoLemmas;
    private final GetDtoIndexUtil getDtoIndex;
    private final Site site;
    private final UrlUtil urlUtil;

    @Override
    public void run() {
        String siteName = site.getName();
        if (siteRepository.findByUrl(site.getUrl()) != null) {
            log.info(ErrorsAndLogsUtil.LOG_DELETE_DATA + siteName);
            deleteData(site);
        }
        log.info(ErrorsAndLogsUtil.LOG_START_INDEXING + siteName);
        try {
            addSiteToTheRepository();
            log.info(ErrorsAndLogsUtil.LOG_ADD_SITE + siteName);

            addPagesToTheRepository();
            log.info(ErrorsAndLogsUtil.LOG_ADD_PAGES + siteName);

            addLemmasToTheRepository();
            log.info(ErrorsAndLogsUtil.LOG_ADD_LEMMAS + siteName);

            addIndexToTheRepository();
        } catch (InterruptedException e) {
            log.info(ErrorsAndLogsUtil.LOG_INDEXING_STOPED);

            List<SiteEntity> sites = siteRepository.findByStatus(SiteStatus.INDEXING);
            for (SiteEntity siteEntity : sites) {
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity.setStatus(SiteStatus.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteRepository.save(siteEntity);
            }
        }
    }

    private void addSiteToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteEntity siteEntity = new SiteEntity();
            siteEntity.setStatus(SiteStatus.INDEXING);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteEntity.setUrl(site.getUrl());
            siteEntity.setName(site.getName());
            siteRepository.save(siteEntity);

        } else {
            throw new InterruptedException();
        }
    }

    private synchronized void addPagesToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {

            ForkJoinPool forkJoinPool = new ForkJoinPool(IndexingServiceImpl.getCoreCount());
            List<PageEntity> pagesStore = new CopyOnWriteArrayList<>();

            CopyOnWriteArrayList<String> linksPool = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<DtoPage> pages = new CopyOnWriteArrayList<>();

            String siteUrl = site.getUrl();
            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);

            CopyOnWriteArrayList<DtoPage> dtoPages = forkJoinPool
                    .invoke(new GetDtoPagesUtil(linksPool, pages, urlUtil, siteUrl));
            for (DtoPage dtoPage : dtoPages) {
                String pageUrl = dtoPage.getPath();
                String path = pageUrl.endsWith("/") ? urlUtil.getPathToPage(pageUrl)
                        : urlUtil.getPathToPage(pageUrl) + "/";

                PageEntity pageEntity = new PageEntity();
                pageEntity.setSiteId(siteEntity);
                pageEntity.setPath(path);
                pageEntity.setCode(dtoPage.getCode());
                pageEntity.setContent(dtoPage.getContent());
                pagesStore.add(pageEntity);
            }
            pageRepository.saveAll(pagesStore);
        } else {
            throw new InterruptedException();
        }
    }

    private void addLemmasToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            List<DtoLemma> dtoLemmas = getDtoLemmas.getLemmas(siteEntity);

            List<LemmaEntity> lemmasStore = new CopyOnWriteArrayList<>();
            for (DtoLemma dtoLemma : dtoLemmas) {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(dtoLemma.getLemma());
                lemmaEntity.setFrequency(dtoLemma.getFrequency());
                lemmasStore.add(lemmaEntity);
            }
            lemmaRepository.saveAll(lemmasStore);
        } else {
            throw new InterruptedException();
        }
    }

    private void addIndexToTheRepository() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
            List<DtoIndex> dtoIndexList = getDtoIndex.getDtoIndexList(siteEntity);

            List<IndexEntity> indexStore = new CopyOnWriteArrayList<>();
            for (DtoIndex dtoIndex : dtoIndexList) {
                PageEntity pageEntity = pageRepository.getById(dtoIndex.getPageId());
                LemmaEntity lemmaEntity = lemmaRepository.getById(dtoIndex.getLemmaId());

                IndexEntity indexEntity = new IndexEntity();
                indexEntity.setPageId(pageEntity);
                indexEntity.setLemmaId(lemmaEntity);
                indexEntity.setRank(dtoIndex.getRank());
                indexStore.add(indexEntity);
            }
            indexRepository.saveAll(indexStore);
            log.info(ErrorsAndLogsUtil.LOG_ADD_INDEX + site.getName());

            siteEntity.setStatus(SiteStatus.INDEXED);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
            log.info(ErrorsAndLogsUtil.LOG_INDEXING_COMPLETED + siteEntity.getName());
        } else {
            throw new InterruptedException();
        }
    }

    private void deleteData(Site site) {
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
        siteEntity.setStatus(SiteStatus.INDEXING);
        siteRepository.save(siteEntity);
        siteRepository.delete(siteEntity);
        log.info(ErrorsAndLogsUtil.LOG_FINISH_DELETE_DATA + site.getName());
    }


}

