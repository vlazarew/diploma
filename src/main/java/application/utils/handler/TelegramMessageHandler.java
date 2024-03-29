package application.utils.handler;

import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.time.LocalDateTime;

public interface TelegramMessageHandler {

    void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation);

    void sendTextMessageReplyKeyboardMarkup(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup,
                                            UserStatus status);

    void sendNewsTwitterMainPageKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status);

    void sendCommonSettingKeyboard(Long chatId, String text, UserStatus status);

    void sendCommonAddDeleteKeyboard(Long chatId, String text, UserStatus status);

    void sendNewsWatchKeyboard(Long chatId, TelegramUser user, UserStatus status, boolean isNextNews);

    void sendTwitterWatchKeyboard(Long chatId, TelegramUser user, UserStatus status, boolean isNextNews);

    void sendWeatherWatchKeyboard(Long chatId, TelegramUser user, UserStatus status, boolean isNextForecast, boolean isUserLocation);

    void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status);

    void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status);

    void sendNewsSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status);

    void sendTextMessageForecastAboutFollowingCities(Long chatId, TelegramUser telegramUser, boolean isUserLocation);

    void saveServiceSettings(TelegramUser telegramUser, boolean isActive, WebService webService);

    void sendTextMessageAddDeleteSomething(Long chatId, String text, UserStatus status);

    void sendTextMessageLastNews(Long chatId, TelegramUser telegramUser, boolean isFollowingNews);

    void sendTwitterSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status);

    void sendTextMessageLastTweets(Long chatId, TelegramUser telegramUser, boolean isFollowingTweets);
}
