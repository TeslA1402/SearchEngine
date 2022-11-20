package searchengine.services.indexing;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import searchengine.services.ingexing.LemmaParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LemmaParserTest {

    @SneakyThrows
    @Test
    void parse() {
        Map<String, Long> expected = new HashMap<>() {{
            put("повторный", 1L);
            put("появление", 1L);
            put("постоянно", 1L);
            put("постоянный", 1L);
            put("позволять", 1L);
            put("предположить", 1L);
            put("северный", 1L);
            put("район", 1L);
            put("кавказ", 1L);
            put("осетия", 1L);
            put("леопард", 2L);
            put("обитать", 1L);
        }};

        LemmaParser lemmaFinder = LemmaParser.getInstance();
        Map<String, Long> lemmas = lemmaFinder.parse("Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.");

        assertEquals(expected, lemmas);
    }
}
