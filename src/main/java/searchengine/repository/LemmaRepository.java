package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Optional;
import java.util.Set;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findBySiteAndLemma(Site site, String lemma);

    Set<Lemma> findAllBySite(Site site);

    long countBySite(Site site);
}
