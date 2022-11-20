package searchengine.services.ingexing;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LemmaParser {

    public static final String REGEX_FOR_NORMALIZE = "[^А-Яа-я]";
    private static final Set<String> PARTICLES_NAMES = Set.of("МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС");
    private final LuceneMorphology luceneMorph;

    public static LemmaParser getInstance() throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        return new LemmaParser(luceneMorph);
    }

    private LemmaParser(LuceneMorphology luceneMorphology) {
        this.luceneMorph = luceneMorphology;
    }

    public Map<String, Long> parse(String text) {
        return Arrays.stream(text.trim().split("\\s+"))
                .map(this::normalize)
                .filter(Predicate.not(String::isBlank))
                .filter(this::notParticle)
                .map(luceneMorph::getNormalForms)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
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
