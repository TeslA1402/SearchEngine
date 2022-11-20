package searchengine.services.ingexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class LemmaFinder {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public void find(Page page) throws IOException {
        LemmaParser parser = LemmaParser.getInstance();
        String content = page.getContent();
        String text = htmlToText(content);
        Map<String, Long> lemmas = parser.parse(text);
        lemmas.forEach((name, count) -> {
            Lemma lemma = lemmaRepository.findBySiteAndLemma(page.getSite(), name).orElseGet(() -> Lemma.builder()
                    .frequency(0)
                    .lemma(name)
                    .site(page.getSite())
                    .build());
            lemma.incrementFrequency();
            Lemma persistLemma = lemmaRepository.save(lemma);
            Index index = Index.builder()
                    .page(page)
                    .lemma(persistLemma)
                    .rank((float) count)
                    .build();
            indexRepository.save(index);
        });

    }

    private String htmlToText(String html) {
        return Jsoup.parse(html).text();
    }
}
