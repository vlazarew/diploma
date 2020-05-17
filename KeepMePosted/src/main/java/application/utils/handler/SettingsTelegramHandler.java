package application.utils.handler;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import com.google.inject.internal.cglib.core.$DuplicatesPredicate;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.List;

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
            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
        } else if (userAnswer.equals(NOTIFICATION_SETTINGS_BUTTON)) {
            sendNotificationSettingsKeyboard(chatId, user, "Настройки периода оповещений", UserStatus.NotificationSettings);
        } else if (userAnswer.equals(WEATHER_SETTINGS_BUTTON)) {
            sendWeatherSettingsMessage(chatId, user, "Настройки рассылки погоды", UserStatus.WeatherSettings);
        } else if (userAnswer.equals(SETTINGS_BACK_BUTTON)) {
            if (user.getStatus() == UserStatus.Settings) {
                sendMessageToUserByCustomMainKeyboard(chatId, user, "Главная страница", UserStatus.MainPage);
            } else if (user.getStatus() == UserStatus.NotificationSettings) {
                sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
            }
        } else if (status == UserStatus.NotificationSettings) {
            updateUserNotificationInterval(user, userAnswer);
            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
        }

    }

    @Transactional
    void updateUserNotificationInterval(TelegramUser user, String text) {
        List<NotificationServiceSettings> notificationServiceSettingsList = notificationServiceSettingsRepository.findByUser(user);
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

        notificationServiceSettingsList.forEach(notificationServiceSettings -> {
            float notificationInterval = intervalDescription.get(text);

            notificationServiceSettings.setNotificationInterval(notificationInterval);
            notificationServiceSettings.setCountOfNotificationPerDay((int) (secondsInDay / notificationInterval));
            notificationServiceSettingsRepository.save(notificationServiceSettings);
        });
    }

    private void sendNotificationSettingsKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNotificationSettingsKeyboardMarkup(telegramUser,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }


}
