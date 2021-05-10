package application.utils.handler;

import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class HelloTelegramMessageHandler extends TelegramHandler {

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        String messageText = telegramMessage.getText();

        if (!messageText.startsWith(START_COMMAND)
                && !messageText.equals(HELLO_BUTTON)) {
            return;
        }


        Long chatId = telegramMessage.getChat().getId();
        TelegramUser telegramUser = telegramMessage.getFrom();
        String text = "Привет, " + telegramUser.getLastName() + " " + telegramUser.getFirstName();

        sendMessageToUserByCustomMainKeyboard(chatId, telegramUser, text, null);
    }
}
