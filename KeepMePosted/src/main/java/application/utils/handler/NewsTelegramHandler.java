package application.utils.handler;

import application.data.model.news.NewsCategory;
import application.data.model.service.NewsSettings;
import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.news.NewsCategoryRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class NewsTelegramHandler extends TelegramHandler {

    @Autowired
    NewsCategoryRepository newsCategoryRepository;

    final WebService webService = WebService.NewsService;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser telegramUser = telegramMessage.getFrom();
        UserStatus status = telegramUser.getStatus();
        String userAnswer = telegramMessage.getText();

        if (userAnswer.equals(SETTINGS_BACK_BUTTON) && status == UserStatus.NewsSettings) {
            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
        } else if (userAnswer.equals(ACTIVATE_NEWS_BUTTON)) {
            saveServiceSettings(telegramUser, true, webService);
            sendNewsSettingsMessage(chatId, telegramUser, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_NEWS_BUTTON)) {
            saveServiceSettings(telegramUser, false, webService);
            sendNewsSettingsMessage(chatId, telegramUser, "Оповещения выключены", null);
        } else if (userAnswer.equals(LIST_FOLLOWING_CATEGORIES_BUTTON)) {
            String messageToUser = listNewsSettingToUser(telegramUser, WebService.NewsService);
            sendNewsSettingsMessage(chatId, telegramUser, messageToUser, null);
        } else if (userAnswer.equals(ADD_CATEGORY_NEWS_BUTTON)) {
            String listOfCities = listNewsSettingToUser(telegramUser, WebService.NewsService);
            String messageToUser = listOfCities + "\r\n\r\n" + "Введите добавляемую тему";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddCategory);
        } else if (userAnswer.equals(REMOVE_CATEGORY_NEWS_BUTTON)) {
            String listOfCities = listNewsSettingToUser(telegramUser, WebService.NewsService);
            String messageToUser = listOfCities + "\r\n\r\n" + "Введите удаляемую тему";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveCategory);
        } else if (userAnswer.equals(CANCEL_BUTTON) && (status == UserStatus.AddCategory || status == UserStatus.RemoveCategory)) {
            String messageToUser = (status == UserStatus.AddCategory) ? "Добавление темы отменено" :
                    "Удаление темы отменено";

            sendNewsSettingsMessage(chatId, telegramUser, messageToUser, UserStatus.NewsSettings);
        } else if (status == UserStatus.AddCategory || status == UserStatus.RemoveCategory) {
            sendAddRemoveCategoryMessageToUser(chatId, telegramUser, userAnswer, status);
        } else if (userAnswer.equals(SHOW_ALL_NEWS)) {
            sendTextMessageLastNews(chatId, telegramUser, false);
        } else if (userAnswer.equals(SHOW_FOLLOWING_NEWS)) {
            sendTextMessageLastNews(chatId, telegramUser, true);
        }
    }

    private void sendAddRemoveCategoryMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddCategory) {
            messageToUser = addNewsSettingsToUser(user, WebService.NewsService, userAnswer)
                    ? "Тема " + userAnswer + " добавлен в список отслеживаемых"
                    : "Тема " + userAnswer + " уже отслеживается вами";
        } else if (status == UserStatus.RemoveCategory) {
            messageToUser = removeNewsSettingsToUser(user, WebService.NewsService, userAnswer)
                    ? "Тема " + userAnswer + " удален из списка отслеживаемых"
                    : "Тема " + userAnswer + " не отслеживался вами";
        } else {
            return;
        }

        sendNewsSettingsMessage(chatId, user, messageToUser, UserStatus.NewsSettings);
    }

    private String listNewsSettingToUser(TelegramUser user, WebService webService) {
        List<NewsSettings> newsSettingsList = newsSettingsRepository.findByUserId(user.getId());
        String headerMessage = newsSettingsList.size() > 0 ? ("Список отслеживаемых тем: " + "\r\n\r\n")
                : "Список отслеживаемых тем пуст";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        newsSettingsList.forEach(newsSettings -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(newsSettings.getKeyword()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    @Transactional
    boolean addNewsSettingsToUser(TelegramUser user, WebService webService, String keyword) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        NewsSettings newsSettings = newsSettingsRepository.findByUserIdAndKeyword(user.getId(), keyword);
        boolean needToCreateNewRule = (newsSettings == null);
        if (needToCreateNewRule) {
            saveNewsSettings(user, keyword, notificationServiceSettings);
        }

        return needToCreateNewRule;
    }

    private void saveNewsSettings(TelegramUser user, String keyword, NotificationServiceSettings notificationServiceSettings) {
        NewsSettings newsSettings = new NewsSettings();
        newsSettings.setKeyword(keyword);
        newsSettings.setUser(user);
        newsSettings.setNotificationServiceSettings(notificationServiceSettings);

        NewsCategory newsCategory = newsCategoryRepository.findByName(keyword);
        newsSettings.setNewsCategory(newsCategory);

        newsSettingsRepository.save(newsSettings);
    }

    @Transactional
    boolean removeNewsSettingsToUser(TelegramUser user, WebService webService, String keyword) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserIdAndKeyword(user.getId(), keyword);
        boolean needToDelete = (newsSettings != null);
        if (needToDelete) {
            newsSettingsRepository.delete(newsSettings);
        }

        return needToDelete;
    }
}
