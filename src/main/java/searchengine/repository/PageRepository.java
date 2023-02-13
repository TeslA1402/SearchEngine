package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Integer> {
    boolean existsBySiteAndPath(Site site, String path);

    long countBySite(Site site);

    Optional<Page> findBySiteAndPath(Site site, String path);
}
