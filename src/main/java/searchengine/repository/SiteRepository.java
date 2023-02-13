package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.Optional;
import java.util.Set;

public interface SiteRepository extends JpaRepository<Site, Integer> {
    Set<Site> findAllByStatus(SiteStatus status);

    boolean existsByStatus(SiteStatus status);

    boolean existsByIdAndStatus(Integer id, SiteStatus status);

    Optional<Site> findByUrlIgnoreCase(String url);

    boolean existsByStatusNot(SiteStatus status);
}
