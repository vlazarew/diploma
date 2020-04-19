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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AutoReplyTelegramMessageHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;

    @Override
    public void handle(TelegramUpdate telegramUpdate) {
        if (telegramUpdate.getMessage().getText().startsWith(TelegramBot.START_COMMAND) ||
                telegramUpdate.getMessage().getText().startsWith(TelegramBot.HELP_BUTTON) ||
                telegramUpdate.getMessage().getText().startsWith(TelegramBot.HELLO_BUTTON)) {
            return;
        }

        Long chatId = telegramUpdate.getMessage().getChat().getId();
        User user = telegramUpdate.getMessage().getFrom();
        String text = Stream.of(user.getLastName(), user.getFirstName(), " сказал: ", telegramUpdate.getMessage().getText())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        telegramBot.sendTextMessage(chatId, text);
    }
}
