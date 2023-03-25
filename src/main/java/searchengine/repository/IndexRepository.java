package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.Set;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    int countByLemma(Lemma lemma);

    Set<Index> findAllByLemmaAndPageIn(Lemma lemma, Set<Page> pages);
}
