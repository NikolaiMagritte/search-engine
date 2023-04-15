package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.utils.LemmaFinderUtil;
import searchengine.services.utils.UrlUtil;

import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class IndexingOnePage {
    private final static String LOG_DELETE_PAGE = "-> Удаляем данные в БД для страницы: ";
    private final static String LOG_ADD_LEMMAS_AND_INDEX = "-> В БД добавлены леммы и index страницы: ";

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final UrlUtil urlUtil;
    private final LemmaFinderUtil lemmaFinder;

    public void start(String page) {
        String path =  page.endsWith("/") ? urlUtil.getPathToPage(page) : urlUtil.getPathToPage(page) + "/";
        String hostSite = urlUtil.getHostFromPage(page);
        SiteEntity siteEntity;

        if (siteRepository.findByUrlLike("%" + hostSite + "%") == null) {
            String name = getNameSite(page);
            String url = getUrlSite(page);
            siteEntity = new SiteEntity();
            siteEntity.setStatus(SiteStatus.INDEXING);
            siteEntity.setStatusTime(new Date());
            siteEntity.setUrl(url);
            siteEntity.setName(name);
            siteRepository.save(siteEntity);
        } else {
            siteEntity = siteRepository.findByUrlLike("%" + hostSite + "%");
        }

        if (pageRepository.findByPath(path) != null) {
            log.info(LOG_DELETE_PAGE + page);
            PageEntity pageEntity = pageRepository.findByPath(path);

            pageRepository.deleteById(pageEntity.getId());
        }

        PageEntity pageEntity = new PageEntity();

        Document document = urlUtil.getConnection(page);
        if (document == null) {
            pageEntity.setSiteId(siteEntity);
            pageEntity.setPath(path);
            pageEntity.setCode(504);
            pageEntity.setContent("GATEAWAY TIMEOUT");
        } else {
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();

            pageEntity.setSiteId(siteEntity);
            pageEntity.setPath(path);
            pageEntity.setCode(code);
            pageEntity.setContent(htmlContent);
        }
        pageRepository.save(pageEntity);
        addLemas(pageEntity, siteEntity);

        log.info(LOG_ADD_LEMMAS_AND_INDEX + page);
    }



    private void addLemas(PageEntity pageEntity, SiteEntity siteEntity) {
        String content = pageEntity.getContent();
        String clearContent = lemmaFinder.removeHtmlTags(content);
        HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
        Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());

        for (String lemma : lemmasSet) {
            float rank = lemmasMap.get(lemma);
            if (lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity) != null) {
                LemmaEntity lemmaEntity = lemmaRepository.findLemmaByLemmaAndSite(lemma, siteEntity);
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                lemmaRepository.save(lemmaEntity);

                addIndex(pageEntity, lemmaEntity, rank);

                siteEntity.setStatus(SiteStatus.INDEXED);
                siteRepository.save(siteEntity);
            } else {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteId(siteEntity);
                lemmaEntity.setLemma(lemma);
                lemmaEntity.setFrequency(1);
                lemmaRepository.save(lemmaEntity);

                addIndex(pageEntity, lemmaEntity, rank);

                siteEntity.setStatus(SiteStatus.INDEXED);
                siteRepository.save(siteEntity);
            }
        }
    }

    private void addIndex(PageEntity pageEntity, LemmaEntity lemmaEntity, float rank) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setPageId(pageEntity);
        indexEntity.setLemmaId(lemmaEntity);
        indexEntity.setRank(rank);
        indexRepository.save(indexEntity);
    }

    private String getUrlSite(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return site.getUrl();
            }
        }
        return "";
    }

    private String getNameSite(String page) {
        for (Site site : sitesList.getSites()) {
            String pageHost = urlUtil.getHostFromPage(page);
            if (site.getUrl().contains(pageHost)) {
                return site.getName();
            }
        }
        return "";
    }

}
