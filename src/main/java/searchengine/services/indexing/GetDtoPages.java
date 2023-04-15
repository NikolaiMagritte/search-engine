package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import searchengine.dto.indexing.DtoPage;
import searchengine.services.utils.UrlUtil;
import searchengine.services.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;


@RequiredArgsConstructor
public class GetDtoPages extends RecursiveTask<CopyOnWriteArrayList<DtoPage>> {
    private final CopyOnWriteArrayList<String> linksPool;
    private final CopyOnWriteArrayList<DtoPage> dtoPages;
    private final UrlUtil urlUtil;
//    private final JsoupConnection jsoupConnection;
    private final String siteUrl;

    @Override
    public CopyOnWriteArrayList<DtoPage> compute() {
        linksPool.add(siteUrl);
        try {
            Thread.sleep(150);
            Document document = urlUtil.getConnection(siteUrl);
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String htmlContent = document.outerHtml();
            DtoPage dtoPage = new DtoPage(siteUrl, code, htmlContent);
            dtoPages.add(dtoPage);
            List<GetDtoPages> tasks = new ArrayList<>();
            Elements elements = document.select("body").select("a");
            for (Element element : elements) {
                String link = element.absUrl("href");
                if (ValidationUtil.isCorrectLink(link) && link.startsWith(element.baseUri())
                        && !linksPool.contains(link)) {
                    linksPool.add(link);
                    GetDtoPages task = new GetDtoPages(linksPool, dtoPages, urlUtil, link);
                    task.fork();
                    tasks.add(task);
                }
            }
            for (GetDtoPages task : tasks) {
                task.join();
            }
        } catch (Exception e) {
            DtoPage dtoPage = new DtoPage(siteUrl, 500, "INTERNAL SERVER ERROR");
            dtoPages.add(dtoPage);
        }
        return dtoPages;
    }

//    private Document getConnection(String url) {
//        try {
//            return Jsoup.connect(url)
//                    .userAgent(jsoupConnection.getUserAgent())
//                    .referrer(jsoupConnection.getReferrer())
//                    .timeout(1000)
//                    .ignoreHttpErrors(true)
//                    .get();
//        } catch (Exception e) {
//            return null;
//        }
//    }
}

