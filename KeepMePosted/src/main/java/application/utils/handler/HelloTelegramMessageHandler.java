package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

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
        // Если не текст или не кнопка "Привет" или старт приложения
        if (!isText || (!telegramUpdate.getMessage().getText().startsWith(TelegramBot.START_COMMAND)
                && !telegramUpdate.getMessage().getText().equals(TelegramBot.HELLO_BUTTON))) {
            return;
        }

        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser telegramUser = telegramUpdate.getMessage().getFrom();
        String text = Stream.of("Привет, ", telegramUser.getLastName(), telegramUser.getFirstName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        telegramBot.sendTextMessage(chatId, text);
    }
}
