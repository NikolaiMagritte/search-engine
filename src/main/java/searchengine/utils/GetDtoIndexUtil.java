package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.indexing.DtoIndex;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetDtoIndexUtil {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaFinderUtil lemmaFinder;

    private CopyOnWriteArrayList<DtoIndex> dtoIndexList;

    public CopyOnWriteArrayList<DtoIndex> getDtoIndexList(SiteEntity siteEntity) {
        dtoIndexList = new CopyOnWriteArrayList<>();

        List<PageEntity> pages = pageRepository.findBySiteId(siteEntity);
        List<LemmaEntity> lemmas = lemmaRepository.findBySiteId(siteEntity);

        for (PageEntity page : pages) {
            if (page.getCode() < 400) {
                int pageId = page.getId();
                String content = page.getContent();
                String clearContent = lemmaFinder.removeHtmlTags(content);
                HashMap<String, Integer> indexMap = lemmaFinder.collectLemmas(clearContent);
                for (LemmaEntity lemmaEntity : lemmas) {
                    int lemmaId = lemmaEntity.getId();
                    String lemmaWord = lemmaEntity.getLemma();
                    if (indexMap.containsKey(lemmaWord)) {
                        float rank = indexMap.get(lemmaWord);
                        dtoIndexList.add(new DtoIndex(pageId, lemmaId, rank));
                    }

                }
            }
        }
        return dtoIndexList;
    }
}
