package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.repository.IndexRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class IndexService {
    private final IndexRepository indexRepository;

    @Transactional
    public void deleteAll() {
        indexRepository.deleteAllInBatch();
    }

    @Transactional
    public void saveAll(Set<Index> indices) {
        indexRepository.saveAll(indices);
    }

    public int countByLemma(Lemma lemma) {
        return indexRepository.countByLemma(lemma);
    }
}
