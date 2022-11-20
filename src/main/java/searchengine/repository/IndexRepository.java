package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;

public interface IndexRepository extends CrudRepository<Index, Integer> {
    @Transactional
    void deleteByPageSiteUrlIgnoreCase(String url);

    int countByPageSiteUrlIgnoreCase(String url);
}
