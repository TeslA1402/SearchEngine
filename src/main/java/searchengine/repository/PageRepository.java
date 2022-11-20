package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Optional;

public interface PageRepository extends CrudRepository<Page, Integer> {
    boolean existsBySiteAndPath(Site site, String path);

    int countBySite(Site site);

    @Transactional
    void deleteBySite(Site site);

    Optional<Page> findBySiteAndPath(Site site, String path);
}
