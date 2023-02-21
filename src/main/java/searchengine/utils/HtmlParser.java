package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.dto.PageInfo;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HtmlParser {
    private final JsoupConfig jsoupConfig;
    private static final Random random = new Random();

    public PageInfo getPageInfo(String url) throws IOException, InterruptedException {
        Connection.Response response = getResponse(url);
        return new PageInfo(response.parse().html(), response.statusCode());
    }

    public Set<String> getPaths(String content) {
        Document document = Jsoup.parse(content);
        return document.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .filter(path -> path.startsWith("/"))
                .collect(Collectors.toSet());
    }


    private Connection.Response getResponse(String url) throws IOException, InterruptedException {
        Thread.sleep(jsoupConfig.getTimeoutMin() + Math.abs(random.nextInt()) %
                jsoupConfig.getTimeoutMax() - jsoupConfig.getTimeoutMin());

        return Jsoup.connect(url)
                .maxBodySize(0)
                .userAgent(jsoupConfig.getUserAgent())
                .referrer(jsoupConfig.getReferrer())
                .header("Accept-Language", "ru")
                .ignoreHttpErrors(true)
                .execute();
    }

    public String htmlToText(String content) {
        return Jsoup.parse(content).text();
    }

    public String getTitle(String content) {
        Document document = Jsoup.parse(content);
        return document.title();
    }
}
