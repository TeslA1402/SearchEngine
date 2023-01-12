package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Site;

public interface IndexRepository extends CrudRepository<Index, Integer> {
    int countByLemmaAndPageSite(Lemma lemma, Site site);
}
