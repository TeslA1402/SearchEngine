package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.Optional;
import java.util.Set;

public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    Optional<Lemma> findBySiteAndLemma(Site site, String lemma);

    @Transactional
    void deleteBySiteUrlIgnoreCase(String url);

    Set<Lemma> findAllBySiteUrlIgnoreCase(String url);
}
