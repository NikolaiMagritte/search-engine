package searchengine.services.utils;

import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Component
public class ValidationUtil {

    public static boolean isCorrectLink(String link) {
        return isCorrectUrl(link) && !isImageLink(link) && !isDocumentLink(link);
    }

    private static boolean isCorrectUrl(String link) {
        return link.matches("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    }

    private static boolean isImageLink(String link) {
        return link.contains(".jpg")
                || link.contains(".jpeg")
                || link.contains(".png")
                || link.contains(".gif")
                || link.contains(".webp");
    }

    private static boolean isDocumentLink(String link) {
        return link.contains(".pdf")
                || link.contains(".eps")
                || link.contains(".xlsx")
                || link.contains(".doc")
                || link.contains(".pptx")
                || link.contains(".docx")
                || link.contains("#")
                || link.contains("?_ga");
    }


}
