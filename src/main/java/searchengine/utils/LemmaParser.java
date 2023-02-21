package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import searchengine.services.lemma.WordLemmas;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LemmaParser {

    public static final String REGEX_FOR_NORMALIZE = "[^А-Яа-я]";
    private static final Set<String> PARTICLES_NAMES = Set.of("МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС");
    private final LuceneMorphology luceneMorph;

    public LemmaParser() throws IOException {
        this.luceneMorph = new RussianLuceneMorphology();
    }

    public Map<String, Long> parseToLemmaWithCount(String text) {
        return parseToWordWithLemmas(text).stream()
                .map(WordLemmas::getLemmas)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public List<WordLemmas> parseToWordWithLemmas(String text) {
        List<WordLemmas> wordLemmas = new ArrayList<>();
        String[] words = text.trim().split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String normalize = normalize(word);
            if (!normalize.isBlank() && notParticle(normalize)) {
                wordLemmas.add(new WordLemmas(word, i, new HashSet<>(luceneMorph.getNormalForms(normalize))));
            }
        }
        return wordLemmas;
    }

    private String normalize(String word) {
        return word.toLowerCase().replaceAll(REGEX_FOR_NORMALIZE, "");
    }

    private boolean notParticle(String word) {
        return luceneMorph.getMorphInfo(word).stream()
                .map(info -> info.split("\\s+"))
                .filter(strings -> strings.length > 1)
                .map(strings -> strings[1])
                .noneMatch(PARTICLES_NAMES::contains);
    }
}
