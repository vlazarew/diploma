package application.utils.handler;

import application.data.model.TelegramUpdate;
import application.data.model.User;
import application.data.repository.PersonRepository;
import application.data.repository.UserRepository;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void handle(TelegramUpdate telegramUpdate) {
        if (!telegramUpdate.getMessage().getText().startsWith(TelegramBot.START_COMMAND) ||
                Objects.nonNull(telegramUpdate.getMessage().getFrom().getPerson())) {
            return;
        }

        String authCode = telegramUpdate.getMessage().getText().replace(TelegramBot.START_COMMAND, "").trim();
        personRepository.findByAuthCode(authCode)
                .ifPresent(person -> {
                    User user = telegramUpdate.getMessage().getFrom();
                    user.setPerson(person);
                    userRepository.save(user);

                    Long chatId = telegramUpdate.getMessage().getChat().getId();
                    String text = "Вы были авторизованы как " + person.getName();
                    telegramBot.sendTextMessage(chatId, text);
                });
    }
}
