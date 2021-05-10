package application.utils.handler;

import application.data.model.news.NewsCategory;
import application.data.model.news.NewsSource;
import application.data.model.service.NewsSettings;
import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.news.NewsCategoryRepository;
import application.data.repository.news.NewsSourceRepository;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.glassfish.grizzly.utils.ArraySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class NewsTelegramHandler extends TelegramHandler {

    @Autowired
    NewsCategoryRepository newsCategoryRepository;

    @Autowired
    NewsSourceRepository newsSourceRepository;

    final WebService webService = WebService.NewsService;

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser telegramUser = telegramMessage.getFrom();
        UserStatus status = telegramUser.getStatus();
        String userAnswer = telegramMessage.getText();
        boolean addingDeletingStatus = (status == UserStatus.AddCategory || status == UserStatus.RemoveCategory ||
                status == UserStatus.AddSource || status == UserStatus.RemoveSource);

        boolean isNewsStatus = (status == UserStatus.NewsMainPage || status == UserStatus.NewsWatch);

        if (userAnswer.equals(BACK_BUTTON)) {
            backButtonHandler(chatId, telegramUser, status);
        } else if (userAnswer.equals(NEWS_BUTTON)) {
            sendNewsTwitterMainPageKeyboard(chatId, telegramUser, "Раздел «Новости»", UserStatus.NewsMainPage);
        } else if (userAnswer.equals(ACTIVATE_NEWS_BUTTON) & status == UserStatus.NewsSettings) {
            saveServiceSettings(telegramUser, true, webService);
            sendNewsSettingsMessage(chatId, telegramUser, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_NEWS_BUTTON) & status == UserStatus.NewsSettings) {
            saveServiceSettings(telegramUser, false, webService);
            sendNewsSettingsMessage(chatId, telegramUser, "Оповещения выключены", null);
        } else if (userAnswer.equals(LIST_FOLLOWING_CATEGORIES_BUTTON)) {
            String messageToUser = listNewsCategoriesToUser(telegramUser);
            sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.CategoriesList);
        } else if (userAnswer.equals(LIST_FOLLOWING_SOURCES_BUTTON)) {
            String messageToUser = listNewsSourcesToUser(telegramUser);
            sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.SourcesList);
        } else if (status == UserStatus.CategoriesList) {
            categoriesListHandler(chatId, telegramUser, userAnswer);
        } else if (status == UserStatus.SourcesList) {
            sourcesListHandler(chatId, telegramUser, userAnswer);
        } else if (userAnswer.equals(CANCEL_BUTTON) && addingDeletingStatus) {
            String messageToUser = getMessageToUser(status);
            sendNewsSettingsMessage(chatId, telegramUser, messageToUser, UserStatus.NewsSettings);
        } else if (addingDeletingStatus) {
            sendAddRemoveMessageToUser(chatId, telegramUser, userAnswer, status);
        } else if (((userAnswer.equals(WATCH_BUTTON) || userAnswer.equals(NEXT_ITEM_BUTTON)) & isNewsStatus)) {
            sendNewsWatchKeyboard(chatId, telegramUser, UserStatus.NewsWatch, true);
        } else if (userAnswer.equals(PREVIOUS_ITEM_BUTTON) & isNewsStatus) {
            sendNewsWatchKeyboard(chatId, telegramUser, UserStatus.NewsWatch, false);
        } else if (userAnswer.equals(EXIT_WATCH_BUTTON) & isNewsStatus) {
            sendNewsTwitterMainPageKeyboard(chatId, telegramUser, "Раздел «Новости»", UserStatus.NewsMainPage);
        } else if (userAnswer.equals(FIRST_ITEM_BUTTON) & isNewsStatus) {
            clearUserNewsHistory(telegramUser, chatId);
        } else if (userAnswer.equals(ACTIVATE_PERSON_SETTINGS) || userAnswer.equals(DEACTIVATE_PERSON_SETTINGS) & isNewsStatus) {
            changeActivityForNews(telegramUser, chatId);
        }
    }

    private String getMessageToUser(UserStatus status) {
        String messageToUser = "";
        if (status == UserStatus.AddCategory) {
            messageToUser = "Добавление категории отменено";
        } else if (status == UserStatus.RemoveCategory) {
            messageToUser = "Удаление категории отменено";
        } else if (status == UserStatus.AddSource) {
            messageToUser = "Добавление источника отменено";
        } else if (status == UserStatus.RemoveSource) {
            messageToUser = "Удаление источника отменено";
        }
        return messageToUser;
    }

    @Transactional
    void changeActivityForNews(TelegramUser telegramUser, Long chatId) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(telegramUser.getId());
        if (newsSettings == null) {
            return;
        }

        newsSettings.setActiveUserSettings(!newsSettings.isActiveUserSettings());
        newsSettingsRepository.save(newsSettings);
        clearUserNewsHistory(telegramUser, chatId);
    }

    private void clearUserNewsHistory(TelegramUser telegramUser, Long chatId) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(telegramUser.getId());
        if (newsSettings == null) {
            return;
        }

        newsSettings.setLastViewedNewsItem(null);
        newsSettings.setLastNewsPublicationDate(new Date());
        newsSettings.setViewedNews(new HashSet<>());
        newsSettingsRepository.save(newsSettings);

        sendNewsWatchKeyboard(chatId, telegramUser, UserStatus.NewsWatch, true);
    }

    private void backButtonHandler(Long chatId, TelegramUser telegramUser, UserStatus status) {
        if (status == UserStatus.NewsMainPage) {
            sendMessageToUserByCustomMainKeyboard(chatId, telegramUser, "Главная страница", UserStatus.MainPage);
        } else if (status == UserStatus.NewsSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки новостей", UserStatus.NewsCommonSettings);
        } else if (status == UserStatus.CategoriesList || status == UserStatus.SourcesList) {
            sendNewsSettingsMessage(chatId, telegramUser, "Настройки рассылки новостей", UserStatus.NewsSettings);
        }
    }

    private void sendAddRemoveMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddCategory) {
            messageToUser = addNewsCategoriesToUser(user, userAnswer)
                    ? "Категория *" + userAnswer + "* добавлена в список отслеживаемых"
                    : "Категория *" + userAnswer + "* уже отслеживается вами";
        } else if (status == UserStatus.RemoveCategory) {
            messageToUser = removeNewsCategoryToUser(user, userAnswer)
                    ? "Категория *" + userAnswer + "* удалена из списка отслеживаемых"
                    : "Категория *" + userAnswer + "* не отслеживалась вами";
        } else if (status == UserStatus.AddSource) {
            messageToUser = addNewsSourceToUser(user, userAnswer)
                    ? "Источник *" + userAnswer + "* добавлена в список забаненных"
                    : "Источник *" + userAnswer + "* уже забанен вами";
        } else if (status == UserStatus.RemoveSource) {
            messageToUser = removeNewsSourceToUser(user, userAnswer)
                    ? "Источник *" + userAnswer + "* удалена из списка забаненных"
                    : "Источник *" + userAnswer + "* не забанен вами";
        } else {
            return;
        }

        UserStatus nextStatus = (status == UserStatus.AddCategory || status == UserStatus.RemoveCategory) ? UserStatus.CategoriesList
                : UserStatus.SourcesList;

        sendCommonAddDeleteKeyboard(chatId, messageToUser, nextStatus);
    }

    //region Category Handlers

    @Transactional
    boolean addNewsCategoriesToUser(TelegramUser user, String keyword) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());
        boolean needToCreateNewRule = (newsSettings == null || keyword.equals(""));
        if (needToCreateNewRule) {
            saveNewsCategory(user, notificationServiceSettings, keyword);
        } else {
            Set<NewsCategory> categories = newsSettings.getNewsCategories();
            NewsCategory category = newsCategoryRepository.findByName(keyword);
            needToCreateNewRule = (!categories.contains(category));

            if (needToCreateNewRule) {
                categories.add(category);
                newsSettings.setNewsCategories(categories);
                newsSettingsRepository.save(newsSettings);
            }
        }

        return needToCreateNewRule;
    }

    private void saveNewsCategory(TelegramUser user, NotificationServiceSettings
            notificationServiceSettings, String keyword) {
        NewsSettings newsSettings = new NewsSettings();
        newsSettings.setUser(user);
        newsSettings.setNotificationServiceSettings(notificationServiceSettings);

        NewsCategory newsCategory = newsCategoryRepository.findByName(keyword);
        Set<NewsCategory> newsCategories = new HashSet<>();
        newsCategories.add(newsCategory);

        newsSettings.setNewsCategories(newsCategories);
        newsSettingsRepository.save(newsSettings);
    }

    @Transactional
    boolean removeNewsCategoryToUser(TelegramUser user, String keyword) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());

        if (newsSettings == null) {
            return false;
        }

        Set<NewsCategory> newsCategories = newsSettings.getNewsCategories();
        NewsCategory category = newsCategoryRepository.findByName(keyword);

        boolean needToDelete = (newsCategories.contains(category));
        if (needToDelete) {
            newsCategories.remove(category);
            newsSettings.setNewsCategories(newsCategories);
            newsSettingsRepository.save(newsSettings);
        }

        return needToDelete;
    }

    private void categoriesListHandler(Long chatId, TelegramUser telegramUser, String userAnswer) {
        if (userAnswer.equals(COMMON_ADD)) {
            String listOfCategories = listNewsCategoriesToUser(telegramUser);
            String messageToUser = listOfCategories + "\r\n\r\n" + "Введите добавляемую категорию";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddCategory);
        } else if (userAnswer.equals(COMMON_DELETE)) {
            String listOfCategories = listNewsCategoriesToUser(telegramUser);
            String messageToUser = listOfCategories + "\r\n\r\n" + "Введите удаляемую категорию";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveCategory);
        }
    }

    private String listNewsCategoriesToUser(TelegramUser telegramUser) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(telegramUser.getId());
        if (newsSettings == null) {
            addNewsCategoriesToUser(telegramUser, "");
            return "*Список отслеживаемых категорий пуст*";
        }

        Set<NewsCategory> newsCategories = newsSettings.getNewsCategories();
        String headerMessage = !newsCategories.isEmpty() ? ("*Список отслеживаемых категорий:* " + "\r\n\r\n")
                : "*Список отслеживаемых категорий пуст*";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        newsCategories.forEach(newsCategory -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(newsCategory.getName()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    //endregion

    //region Sources Handlers

    @Transactional
    boolean addNewsSourceToUser(TelegramUser user, String keyword) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());
        boolean needToCreateNewRule = (newsSettings == null || keyword.equals(""));
        if (needToCreateNewRule) {
            saveNewsSource(user, notificationServiceSettings, keyword);
        } else {
            Set<NewsSource> sources = newsSettings.getNewsSources();
            NewsSource source = newsSourceRepository.findByName(keyword);
            needToCreateNewRule = (!sources.contains(source));

            if (needToCreateNewRule) {
                sources.add(source);
                newsSettings.setNewsSources(sources);
                newsSettingsRepository.save(newsSettings);
            }
        }

        return needToCreateNewRule;
    }

    private void saveNewsSource(TelegramUser user, NotificationServiceSettings
            notificationServiceSettings, String keyword) {
        NewsSettings newsSettings = new NewsSettings();
        newsSettings.setUser(user);
        newsSettings.setNotificationServiceSettings(notificationServiceSettings);

        NewsSource newsSource = newsSourceRepository.findByName(keyword);
        Set<NewsSource> newsSources = new HashSet<>();
        newsSources.add(newsSource);

        newsSettings.setNewsSources(newsSources);
        newsSettingsRepository.save(newsSettings);
    }

    @Transactional
    boolean removeNewsSourceToUser(TelegramUser user, String keyword) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());

        if (newsSettings == null) {
            return false;
        }

        Set<NewsSource> newsSources = newsSettings.getNewsSources();
        NewsSource source = newsSourceRepository.findByName(keyword);

        boolean needToDelete = (newsSources.contains(source));
        if (needToDelete) {
            newsSources.remove(source);
            newsSettings.setNewsSources(newsSources);
            newsSettingsRepository.save(newsSettings);
        }

        return needToDelete;
    }

    private void sourcesListHandler(Long chatId, TelegramUser telegramUser, String userAnswer) {
        if (userAnswer.equals(COMMON_BANNED_NEWS_SOURCES_ADD)) {
            String listOfSources = listNewsSourcesToUser(telegramUser);
            String messageToUser = listOfSources + "\r\n\r\n" + "Введите запрещаемый источник";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddSource);
        } else if (userAnswer.equals(COMMON_BANNED_NEWS_SOURCES_DELETE)) {
            String listOfSources = listNewsSourcesToUser(telegramUser);
            String messageToUser = listOfSources + "\r\n\r\n" + "Введите разрешаемый источник";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveSource);
        }
    }

    private String listNewsSourcesToUser(TelegramUser telegramUser) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(telegramUser.getId());
        if (newsSettings == null) {
            addNewsSourceToUser(telegramUser, "");
            return "*Список забаненных источников пуст*";
        }

        Set<NewsSource> newsSources = newsSettings.getNewsSources();
        String headerMessage = !newsSources.isEmpty() ? ("*Список забаненных источников:* " + "\r\n\r\n")
                : "*Список забаненных источников пуст*";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        newsSources.forEach(newsSource -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(newsSource.getName()).append("\r\n");
        });

        Iterable<NewsSource> allNewsSources = newsSourceRepository.findAll();
        stringBuilder.append("\r\n\r\n*Список всех источников: *\r\n");
        count.set(0);
        allNewsSources.forEach(newsSource -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(newsSource.getName()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    //endregion
}
