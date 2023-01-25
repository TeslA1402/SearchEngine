package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;

public interface IndexRepository extends CrudRepository<Index, Integer> {
    int countByLemma(Lemma lemma);
}
