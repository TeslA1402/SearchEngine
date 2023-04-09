package searchengine.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.dto.PageInfo;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
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
                .sslSocketFactory(socketFactory())
                .execute();
    }

    private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }

    public String htmlToText(String content) {
        return Jsoup.parse(content).text();
    }

    public String getTitle(String content) {
        Document document = Jsoup.parse(content);
        return document.title();
    }
}
