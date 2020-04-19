package application.utils.handler;

import application.data.model.TelegramUpdate;
import application.data.model.User;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HelpTelegramMessageHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;

    @Override
    public void handle(TelegramUpdate telegramUpdate) {
        if (!telegramUpdate.getMessage().getText().startsWith(TelegramBot.HELP_BUTTON)) {
            return;
        }

        Long chatId = telegramUpdate.getMessage().getChat().getId();
        String text;
        if (Objects.isNull(telegramUpdate.getMessage().getFrom().getPerson())) {
            text = "Мы помогаем только авторизированным пользователям";
        } else {
            text = "Мы поможем тебе";
        }
        telegramBot.sendTextMessage(chatId, text);
    }
}