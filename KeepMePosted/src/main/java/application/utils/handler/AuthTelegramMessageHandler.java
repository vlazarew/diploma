package application.utils.handler;

import application.data.model.TelegramUpdate;
import application.data.model.TelegramUser;
import application.data.repository.PersonRepository;
import application.data.repository.UserRepository;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthTelegramMessageHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    PersonRepository personRepository;
    UserRepository userRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        if (!isText || !telegramUpdate.getMessage().getText().startsWith(TelegramBot.START_COMMAND) ||
                Objects.nonNull(telegramUpdate.getMessage().getFrom().getPerson())) {
            return;
        }

        String authCode = telegramUpdate.getMessage().getText().replace(TelegramBot.START_COMMAND, "").trim();
        personRepository.findByAuthCode(authCode)
                .ifPresent(person -> {
                    TelegramUser telegramUser = telegramUpdate.getMessage().getFrom();
                    telegramUser.setPerson(person);
                    userRepository.save(telegramUser);

                    Long chatId = telegramUpdate.getMessage().getChat().getId();
                    String text = "Вы были авторизованы как " + person.getName();
                    telegramBot.sendTextMessage(chatId, text);
                });
    }
}
