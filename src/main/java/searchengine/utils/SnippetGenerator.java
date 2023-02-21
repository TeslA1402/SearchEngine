package searchengine.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.lemma.WordLemmas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SnippetGenerator {
    private static final int SYMBOLS_IN_SNIPPET = 180;
    private final LemmaParser lemmaParser;
    private final HtmlParser htmlParser;

    public String generateSnippet(String query, String content) {
        List<WordLemmas> queryLemmas = lemmaParser.parseToWordWithLemmas(query);
        List<WordLemmas> wordLemmas = lemmaParser.parseToWordWithLemmas(htmlParser.htmlToText(content));
        List<WordLemmas> pageCommonSequence = commonSequence(queryLemmas, wordLemmas);
        return paddingSnippet(content, pageCommonSequence);
    }

    private String paddingSnippet(String content, List<WordLemmas> pageCommonSequence) {
        String[] split = htmlParser.htmlToText(content).trim().split("\\s+");
        int startIndex = pageCommonSequence.get(0).getIndex();
        int endIndex = pageCommonSequence.get(pageCommonSequence.size() - 1).getIndex() + 1;
        int length = 0;
        int leftIndex = startIndex;
        int rightIndex = endIndex;
        while (length < SYMBOLS_IN_SNIPPET) {
            if (leftIndex > 0) {
                leftIndex--;
                length += split[leftIndex].length() + 1;
            }
            if (rightIndex < split.length) {
                length += split[rightIndex].length() + 1;
                rightIndex++;
            }
            if (startIndex == 0 && endIndex == split.length) {
                break;
            }
        }
        return (String.join(" ", Arrays.asList(split).subList(leftIndex, startIndex)) + " <b>" +
                String.join(" ", Arrays.asList(split).subList(startIndex, endIndex)) + "</b> " +
                String.join(" ", Arrays.asList(split).subList(endIndex, rightIndex))).trim();
    }

    private List<WordLemmas> commonSequence(List<WordLemmas> query, List<WordLemmas> page) {
        List<List<WordLemmas>> buffer = new ArrayList<>();
        for (int i = 0; i < page.size(); i++) {
            for (int j = 0; j < query.size(); j++) {
                if (page.get(i).equals(query.get(j))) {
                    List<WordLemmas> list = new ArrayList<>();
                    int k = 0;
                    while (i + k < page.size() && j + k < query.size() && page.get(i + k).equals(query.get(j + k))) {
                        list.add(page.get(i + k));
                        k++;
                    }
                    buffer.add(list);
                }
            }
        }
        return buffer.stream().max(Comparator.comparingInt(List::size))
                .orElseThrow(() -> new IllegalStateException("Common sequence not found"));
    }
}
