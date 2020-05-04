package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface TelegramMessageHandler {

    void sendTextMessageReplyKeyboardMarkup(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup,
                                            UserStatus status);

    void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation);

    void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text);

    void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status);

    void sendSettingsKeyboard(Long chatId, String text);

    void sendSettingsKeyboard(Long chatId, String text, UserStatus status);

    void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text);

    void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status);

    void sendTextMessageAddDeleteCity(Long chatId, TelegramUser user, String text, UserStatus status);

}
