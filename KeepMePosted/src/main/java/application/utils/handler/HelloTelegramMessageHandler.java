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
public class HelloTelegramMessageHandler extends AbstractTelegramHandler {

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        String messageText = telegramMessage.getText();

        if (!messageText.startsWith(TelegramBot.START_COMMAND)
                && !messageText.equals(TelegramBot.HELLO_BUTTON)) {
            return;
        }


        Long chatId = telegramMessage.getChat().getId();
        TelegramUser telegramUser = telegramMessage.getFrom();
        String text = "Привет, " + telegramUser.getLastName() + " " + telegramUser.getFirstName();

        sendMessageToUserByCustomMainKeyboard(chatId, telegramUser, text);
    }
}
