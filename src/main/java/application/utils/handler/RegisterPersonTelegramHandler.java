package application.utils.handler;

import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.validator.EmailValidator;
import org.checkerframework.checker.units.qual.A;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class RegisterPersonTelegramHandler extends TelegramHandler {

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser user = telegramMessage.getFrom();
        UserStatus status = user.getStatus();

        if (hasContact) {
            askUsersEmail(chatId, "Введите свой e-mail адрес (обязательно)");
            return;
        }

        if (!hasText) {
            return;
        }

        String userAnswer = telegramMessage.getText();
        if (userAnswer.equals(REGISTER_BUTTON)) {
            askUserPhone(chatId, "Поделитесь номером телефона (опционально)");
        } else if (userAnswer.equals(NEXT_BUTTON)) {
            askUsersEmail(chatId, "Введите свой e-mail адрес (обязательно)");
        } else if (userAnswer.equals(CANCEL_REGISTRATION_BUTTON)) {
            sendMessageToUserByCustomMainKeyboard(chatId, user, "Регистрация отменена", UserStatus.NotRegistered);
        } else if (status == UserStatus.VerifyEmail) {
            checkUserEmail(chatId, user, userAnswer);
        }

    }

    private void askUsersEmail(Long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAskUsersEmailReplyKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, UserStatus.VerifyEmail);
    }

    private void askUserPhone(Long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAskUsersPhoneReplyKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, UserStatus.VerifyPhone);
    }

    private void checkUserEmail(Long chatId, TelegramUser user, String text) {
        if (isValid(text)) {
            user.setStatus(UserStatus.MainPage);
            user.setRegistered(true);
            user.setEmail(text);
            userRepository.save(user);

            sendMessageToUserByCustomMainKeyboard(chatId, user, "Успешная регистрация!", null);
        } else {
            askUsersEmail(chatId, "Введен некорректный email. Повторите ввод");
        }
    }

    private boolean isValid(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
