package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    void deleteBySiteId(SiteEntity siteEntity);

    List<PageEntity> findBySiteId(SiteEntity siteEntity);

    PageEntity findByPath(String path);

    int countBySiteId(SiteEntity siteId);

    @Transactional
    void deleteByPath(String path);

    @Query(value = "SELECT p.* FROM page p JOIN search_index i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas",
            nativeQuery = true)
    List<PageEntity> findByLemmas(@Param("lemmas") Collection<LemmaEntity> lemmas);
}
