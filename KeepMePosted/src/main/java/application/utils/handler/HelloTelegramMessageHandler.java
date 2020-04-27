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

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HelloTelegramMessageHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        if (!isText) {
            return;
        }

        String messageText = telegramUpdate.getMessage().getText();
        if (!messageText.startsWith(TelegramBot.START_COMMAND)) {
            return;
        }

        if (!messageText.equals(TelegramBot.HELLO_BUTTON)) {
            return;
        }

        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser telegramUser = telegramUpdate.getMessage().getFrom();
        String text = Stream.of("Привет, ", telegramUser.getLastName(), telegramUser.getFirstName()) // +
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        // method
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, telegramBot, null,
                null, null);
    }
}
