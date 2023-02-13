package searchengine.services.lemma;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
@AllArgsConstructor
public class WordLemmas {
    private String word;
    private int index;
    private Set<String> lemmas;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordLemmas that = (WordLemmas) o;
        return Objects.equals(word, that.word) || lemmas.stream().anyMatch(lemma -> that.lemmas.contains(lemma));
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
