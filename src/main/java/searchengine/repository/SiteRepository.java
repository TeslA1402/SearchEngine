package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.Optional;
import java.util.Set;

public interface SiteRepository extends CrudRepository<Site, Integer> {
    @Transactional
    void deleteByUrlIgnoreCase(String url);

    Set<Site> findAllByStatus(SiteStatus status);

    boolean existsByStatus(SiteStatus status);

    boolean existsByIdAndStatus(Integer id, SiteStatus status);

    Optional<Site> findByUrlIgnoreCase(String url);
}
