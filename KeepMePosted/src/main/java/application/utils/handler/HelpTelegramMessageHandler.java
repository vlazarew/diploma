package application.utils.handler;

import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HelpTelegramMessageHandler extends AbstractTelegramHandler{

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        // Если не текст и не кнопка "Помощь"
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        String messageText = telegramMessage.getText();

        if (!messageText.startsWith(TelegramBot.HELP_BUTTON)) {
            return;
        }

        Long chatId = telegramMessage.getChat().getId();
        TelegramUser user = telegramMessage.getFrom();
        String text = (user.getRegistered() != null && user.getRegistered()) ? "Мы поможем тебе" :
                "Мы помогаем только авторизированным пользователям";

        sendMessageToUserByCustomMainKeyboard(chatId, user, text);
    }
}