package application.utils.handler;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
@EnableAsync
public class SettingsTelegramHandler extends TelegramHandler {

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser user = telegramMessage.getFrom();
        UserStatus status = user.getStatus();

        if (!hasText) {
            return;
        }

        String userAnswer = telegramMessage.getText();


        if (userAnswer.equals(SETTINGS_BUTTON)) {
            settingButtonHandler(chatId, status);
        } else if (userAnswer.equals(COMMON_SETTINGS_BUTTON)) {
            commonSettingHandler(chatId, user, status);
        } else if (userAnswer.equals(NOTIFICATION_SETTINGS_BUTTON)) {
            HashMap<UserStatus, UserStatus> userStatusToNext = new HashMap<UserStatus, UserStatus>() {{
                put(UserStatus.NewsCommonSettings, UserStatus.NewsNotificationSettings);
                put(UserStatus.TwitterCommonSettings, UserStatus.TwitterNotificationSettings);
                put(UserStatus.WeatherCommonSettings, UserStatus.WeatherNotificationSettings);
            }};
            sendNotificationSettingsKeyboard(chatId, user, userStatusToNext.get(status));
        } else if (userAnswer.equals(BACK_BUTTON)) {
            backButtonHandler(chatId, user, status);
        } else if (status == UserStatus.NewsNotificationSettings ||
                status == UserStatus.TwitterNotificationSettings ||
                status == UserStatus.WeatherNotificationSettings) {

            HashMap<UserStatus, WebService> userStatusToWebService = new HashMap<UserStatus, WebService>() {
                {
                    put(UserStatus.NewsNotificationSettings, WebService.NewsService);
                    put(UserStatus.TwitterNotificationSettings, WebService.TwitterService);
                    put(UserStatus.WeatherNotificationSettings, WebService.YandexWeather);
                }
            };

            updateUserNotificationInterval(user, userAnswer, userStatusToWebService.get(status));
            backButtonHandler(chatId, user, status);
        }
    }

    private void settingButtonHandler(Long chatId, UserStatus status) {
        HashMap<UserStatus, String> userStatusTextMessage = new HashMap<UserStatus, String>() {{
            put(UserStatus.NewsMainPage, "Настройки новостей");
            put(UserStatus.TwitterMainPage, "Настройки Twitter");
            put(UserStatus.WeatherMainPage, "Настройки погоды");
        }};

        HashMap<UserStatus, UserStatus> userStatusToNext = new HashMap<UserStatus, UserStatus>() {{
            put(UserStatus.NewsMainPage, UserStatus.NewsCommonSettings);
            put(UserStatus.TwitterMainPage, UserStatus.TwitterCommonSettings);
            put(UserStatus.WeatherMainPage, UserStatus.WeatherCommonSettings);
        }};

        sendCommonSettingKeyboard(chatId, userStatusTextMessage.get(status), userStatusToNext.get(status));
    }

    private void commonSettingHandler(Long chatId, TelegramUser user, UserStatus status) {
        if (status == UserStatus.NewsCommonSettings) {
            sendNewsSettingsMessage(chatId, user, "Настройки рассылки новостей", UserStatus.NewsSettings);
        } else if (status == UserStatus.TwitterCommonSettings) {
            sendTwitterSettingsMessage(chatId, user, "Настройки Twitter", UserStatus.TwitterSettings);
        } else if (status == UserStatus.WeatherCommonSettings) {
            sendWeatherSettingsMessage(chatId, user, "Настройки рассылки погоды", UserStatus.WeatherSettings);
        }
    }

    private void backButtonHandler(Long chatId, TelegramUser user, UserStatus status) {
        if (status == UserStatus.NewsCommonSettings) {
            sendNewsTwitterMainPageKeyboard(chatId, user, "Раздел «Новости»", UserStatus.NewsMainPage);
        } else if (status == UserStatus.TwitterCommonSettings) {
            sendNewsTwitterMainPageKeyboard(chatId, user, "Раздел «Twitter»", UserStatus.TwitterMainPage);
        } else if (status == UserStatus.WeatherCommonSettings) {
            sendNewsTwitterMainPageKeyboard(chatId, user, "Раздел «Погода»", UserStatus.WeatherMainPage);
        } else if (status == UserStatus.NewsNotificationSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки новостей", UserStatus.NewsCommonSettings);
        } else if (status == UserStatus.TwitterNotificationSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки Twitter", UserStatus.TwitterCommonSettings);
        } else if (status == UserStatus.WeatherNotificationSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки погоды", UserStatus.WeatherCommonSettings);
        }
    }

    @Transactional
    void updateUserNotificationInterval(TelegramUser user, String text, WebService webService) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);
        float secondsInDay = 86400f;
        HashMap<String, Float> intervalDescription = new HashMap<String, Float>() {
            {
                put(NOTIFICATION_15_MINUTES, 900F);
                put(NOTIFICATION_30_MINUTES, 1800F);
                put(NOTIFICATION_1_HOUR, 3600F);
                put(NOTIFICATION_2_HOURS, 7200F);
                put(NOTIFICATION_3_HOURS, 10800F);
                put(NOTIFICATION_6_HOURS, 21600F);
                put(NOTIFICATION_9_HOURS, 32400F);
                put(NOTIFICATION_12_HOURS, 43200F);
                put(NOTIFICATION_24_HOURS, 86400F);
            }
        };


        try {
            float notificationInterval = intervalDescription.get(text);
            notificationServiceSettings.setNotificationInterval(notificationInterval);
            notificationServiceSettings.setCountOfNotificationPerDay((int) (secondsInDay / notificationInterval));
            notificationServiceSettingsRepository.save(notificationServiceSettings);
        } catch (Exception e) {
            log.info("Пользователь " + user + " выбрал уже текущую настройку оповещений.");
        }
    }

    private void sendNotificationSettingsKeyboard(Long chatId, TelegramUser telegramUser, UserStatus
            status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNotificationSettingsKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, "Настройки периода оповещений", replyKeyboardMarkup, status);
    }


}
