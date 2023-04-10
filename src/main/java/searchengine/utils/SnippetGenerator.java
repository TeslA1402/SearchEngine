package searchengine.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.services.lemma.WordLemmas;

import java.util.Arrays;
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
        List<WordLemmas> pageCommonSequence = longestCS(queryLemmas, wordLemmas);
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

    private List<WordLemmas> longestCS(List<WordLemmas> query, List<WordLemmas> page) {
        int[][] matrix = new int[query.size()][];

        int maxLength = 0;
        int maxJ = 0;

        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new int[page.size()];
            for (int j = 0; j < matrix[i].length; j++) {
                if (query.get(i).equals(page.get(j))) {
                    if (i != 0 && j != 0) {
                        matrix[i][j] = matrix[i - 1][j - 1] + 1;
                    } else {
                        matrix[i][j] = 1;
                    }
                    if (matrix[i][j] > maxLength) {
                        maxLength = matrix[i][j];
                        maxJ = j;
                    }
                }
            }
        }
        return page.subList(maxJ - maxLength + 1, maxJ + 1);
    }
}
