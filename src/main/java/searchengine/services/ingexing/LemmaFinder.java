package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class LemmaFinder {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public void find(Page page) throws IOException {
        LemmaParser parser = LemmaParser.getInstance();
        String content = page.getContent();
        String text = htmlToText(content);
        Map<String, Long> lemmas = parser.parse(text);
        Set<Lemma> lemmaSetToSave = new HashSet<>();
        Set<Index> indices = new HashSet<>();
        synchronized (lemmaRepository) {
            lemmas.forEach((name, count) -> {
                Optional<Lemma> optionalLemma = lemmaRepository.findBySiteAndLemma(page.getSite(), name);
                Lemma lemma;
                if (optionalLemma.isPresent()) {
                    lemma = optionalLemma.get();
                } else {
                    lemma = Lemma.builder()
                            .frequency(0)
                            .lemma(name)
                            .site(page.getSite())
                            .build();
                    lemmaSetToSave.add(lemma);
                }

                indices.add(Index.builder()
                        .page(page)
                        .lemma(lemma)
                        .rank((float) count)
                        .build());
            });
            lemmaRepository.saveAll(lemmaSetToSave);
        }
        indexRepository.saveAll(indices);
    }

    private String htmlToText(String html) {
        return Jsoup.parse(html).text();
    }
}
