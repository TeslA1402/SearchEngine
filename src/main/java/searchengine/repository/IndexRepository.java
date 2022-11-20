package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

public interface IndexRepository extends CrudRepository<Index, Integer> {
    @Transactional
    void deleteByPageSite(Site site);

    @Transactional
    void deleteByPage(Page page);

    int countByLemma(Lemma lemma);
}
