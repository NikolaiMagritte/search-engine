package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.dto.indexing.DtoIndex;
import searchengine.dto.indexing.DtoLemma;
import searchengine.dto.indexing.DtoPage;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.utils.UrlUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class IndexingAllPages implements Runnable {
    private final static String LOG_ADD_SITE = "-> В БД добавлен сайт: ";
    private final static String LOG_ADD_PAGES = "-> В БД добавлены страницы с сайта: ";
    private final static String LOG_ADD_LEMMAS = "-> В БД добавлены леммы с сайта: ";
    private final static String LOG_ADD_INDEX = "-> В БД добавлены index для сайта: ";
    private final static String LOG_DELETE_DATA = "-> Из БД удаляются старые данные по сайту:  ";
    private final static String LOG_START_INDEXING = "-> Запущена индексация сайта: ";
    private final static String LOG_INDEXING_COMPLETED = "-> Индексация завершена: ";
    private final static String LOG_INDEXING_STOPED = "-> Остановка индексации завершена.";


    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GetDtoLemmas getDtoLemmas;
    private final GetDtoIndex getDtoIndex;
    private final Site site;
    private final UrlUtil urlUtil;

    @Override
    public void run() {
        String siteName = site.getName();
        if (siteRepository.findByUrl(site.getUrl()) != null) {
            log.info(LOG_DELETE_DATA + siteName);
            deleteData(site);
        }
        log.info(LOG_START_INDEXING + siteName);
        try {
            addSiteToTheRepository();
            log.info(LOG_ADD_SITE + siteName);

            addPagesToTheRepository();
            log.info(LOG_ADD_PAGES + siteName);

            addLemmasToTheRepository();
            log.info(LOG_ADD_LEMMAS + siteName);

            addIndexToTheRepository();
        } catch (InterruptedException e) {
            log.info(LOG_INDEXING_STOPED);

            List<SiteEntity> sites = siteRepository.findByStatus(SiteStatus.INDEXING);
            for (SiteEntity siteEntity : sites) {
                siteEntity.setStatusTime(new Date());
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
            siteEntity.setStatusTime(new Date());
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
                    .invoke(new GetDtoPages(linksPool, pages, urlUtil, siteUrl));
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
            log.info(LOG_ADD_INDEX + site.getName());

            siteEntity.setStatus(SiteStatus.INDEXED);
            siteEntity.setStatusTime(new Date());
            siteRepository.save(siteEntity);
            log.info(LOG_INDEXING_COMPLETED + siteEntity.getName());
        } else {
            throw new InterruptedException();
        }
    }

    private void deleteData(Site site) {
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
        siteRepository.delete(siteEntity);
    }


}

