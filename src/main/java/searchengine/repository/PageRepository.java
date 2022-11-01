package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

public interface PageRepository extends CrudRepository<Page, Integer> {
    boolean existsBySiteAndPath(Site site, String path);

    @Transactional
    void deleteBySiteUrlIgnoreCase(String url);
}
