package searchengine.utils;

public interface ErrorsAndLogsUtil {
    //Errors ApiController
    String ERROR_INDEXING_ALREADY_STARTED = "Индексация уже запущена";
    String ERROR_INDEXING_NOT_STARTED = "Индексация не запущена";
    String ERROR_NOT_AVAIBLE_PAGE = "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";

    //Logs IndexingServiceImpl
    String LOG_STOP_INDEXING = "-> Запускаем остановку индексации";
    String LOG_START_INDEXING_PAGE = "-> Запущена индексация страницы: ";
    String LOG_DONE_INDEXING_PAGE = "-> Завершена индексация страницы: ";
    String LOG_NOT_AVAIBLE_PAGE = "Данная страница находится за пределами сайтов, " +
            "указанных в конфигурационном файле";

    //Erorrs and Logs SearchServiceImpl
    String LOG_START_ALLSITES_SEARCH = "-> Запускаем поиск по сайтам для запроса: ";
    String LOG_FINISH_ALLSITES_SEARCH = "-> Поиск по сайтам завершен.";
    String LOG_START_ONESITE_SEARCH = "-> Запускаем поиск по сайту для запроса: ";
    String LOG_FINISH_ONESITES_SEARCH = "-> Поиск по сайту завершен.";

    String ERROR_EMPTY_QUERY = "Задан пустой поисковый запрос";
    String ERROR_NOT_FOUND = "По данному запросу ни чего не найдено.";

    //Logs IndexingAllPages, IndexingOnePage
    String LOG_ADD_SITE = "-> В БД добавлен сайт: ";
    String LOG_ADD_PAGES = "-> В БД добавлены страницы с сайта: ";
    String LOG_ADD_LEMMAS = "-> В БД добавлены леммы с сайта: ";
    String LOG_ADD_INDEX = "-> В БД добавлены index для сайта: ";
    String LOG_DELETE_DATA = "-> Из БД удаляются старые данные по сайту:  ";
    String LOG_FINISH_DELETE_DATA = "-> Из БД удалены данные по сайту:  ";
    String LOG_START_INDEXING = "-> Запущена индексация сайта: ";
    String LOG_INDEXING_COMPLETED = "-> Индексация завершена: ";
    String LOG_INDEXING_STOPED = "-> Остановка индексации завершена.";

    String LOG_DELETE_PAGE = "-> Удаляем данные в БД для страницы: ";
    String LOG_ADD_LEMMAS_AND_INDEX = "-> В БД добавлены леммы и index страницы: ";

    //Logs UrlUtil
    String LOG_MALFORMED_EXCEPTION = "! Некорректный URL адрес: ";
}
