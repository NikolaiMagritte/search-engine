package searchengine.services.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupConnection;

import java.net.MalformedURLException;
import java.net.URL;

@Component
@Slf4j
@RequiredArgsConstructor
public class UrlUtil {
    private final static String LOG_MALFORMED_EXCEPTION = "! Некорректный URL адрес: ";

    private final JsoupConnection jsoupConnection;

    public Document getConnection(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(jsoupConnection.getUserAgent())
                    .referrer(jsoupConnection.getReferrer())
//                    .timeout(1000)
//                    .ignoreHttpErrors(true)
                    .get();
        } catch (Exception e) {
            return null;
        }
    }

    public String getPathToPage(String page) {
        try {
            URL url = new URL(page);
            return url.getPath();
        } catch (MalformedURLException e) {
            log.error(LOG_MALFORMED_EXCEPTION + page);
            return "";
        }
    }

    public String getHostFromPage(String page) {
        try {
            URL url = new URL(page);
            return url.getHost();
        } catch (MalformedURLException e) {
            log.error(LOG_MALFORMED_EXCEPTION + page);
            return "";
        }
    }

    public String getTitleFromHtml(String content) {
        Document doc = Jsoup.parse(content);
        return doc.title();
    }
}
