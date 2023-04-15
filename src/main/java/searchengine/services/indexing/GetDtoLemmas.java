package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.indexing.DtoLemma;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.services.utils.LemmaFinderUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class GetDtoLemmas {
    private final PageRepository pageRepository;
    private final LemmaFinderUtil lemmaFinder;
    private CopyOnWriteArrayList<DtoLemma> dtoLemmas;

    public CopyOnWriteArrayList<DtoLemma> getLemmas(SiteEntity site) {
        dtoLemmas = new CopyOnWriteArrayList<>();
        List<PageEntity> pages = pageRepository.findBySiteId(site);
        HashMap<String, Integer> lemmaspPerPage = new HashMap<>();

        for (PageEntity page : pages) {
            String content = page.getContent();
            String clearContent = lemmaFinder.removeHtmlTags(content);
            HashMap<String, Integer> lemmasMap = lemmaFinder.collectLemmas(clearContent);
            Set<String> lemmasSet = new HashSet<>(lemmasMap.keySet());
            for (String lemma : lemmasSet) {
                int frequency = lemmaspPerPage.getOrDefault(lemma, 0) + 1;
                lemmaspPerPage.put(lemma, frequency);
            }
        }

        for (Map.Entry<String, Integer> entry : lemmaspPerPage.entrySet()) {
            String lemma = entry.getKey();
            int frequency = entry.getValue();
            dtoLemmas.add(new DtoLemma(lemma, frequency));
        }
        return dtoLemmas;
    }
}
