package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository <SiteEntity, Integer> {
    SiteEntity findByUrl(String url);
    SiteEntity findByUrlLike(String url);
    List<SiteEntity> findByStatus(SiteStatus status);
    boolean existsByStatus(SiteStatus status);
}
