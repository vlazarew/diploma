package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import application.telegram.TelegramSendMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HelpTelegramMessageHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        // Если не текст и не кнопка "Помощь"
        if (!isText || !telegramUpdate.getMessage().getText().startsWith(TelegramBot.HELP_BUTTON)) {
            return;
        }

        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        String text = (telegramUpdate.getMessage().getFrom().getRegistered() != null &&
                telegramUpdate.getMessage().getFrom().getRegistered()) ? "Мы поможем тебе" :
                "Мы помогаем только авторизированным пользователям";

        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, telegramBot, null,
                null, null);
    }
}