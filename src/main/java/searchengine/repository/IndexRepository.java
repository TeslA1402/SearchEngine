package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    int countByLemma(Lemma lemma);
}
