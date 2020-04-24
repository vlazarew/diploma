package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import application.telegram.TelegramSendMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RegisterPersonTelegramHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    TelegramUserRepository userRepository;
    TelegramChatRepository telegramChatRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isContact) {
            askUsersEmail(chatId, "Введите свой e-mail адрес (обязательно)");
        } else if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.REGISTER_BUTTON: {
                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getAskUsersPhoneReplyKeyboardMarkup();
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, "Поделитесь номером телефона (опционально)",
                            replyKeyboardMarkup, telegramBot, UserStatus.VerifyPhone, userRepository, telegramChatRepository);
                    break;
                }
                case TelegramBot.NEXT_BUTTON: {
                    askUsersEmail(chatId, "Введите свой e-mail адрес (обязательно)");
                    break;
                }
                case TelegramBot.CANCEL_REGISTRATION_BUTTON: {
                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, "Регистрация отменена",
                            replyKeyboardMarkup, telegramBot, UserStatus.NotRegistered, userRepository, telegramChatRepository);

                    break;
                }
                default: {
                    if (status == UserStatus.VerifyEmail) {
                        checkUserEmail(chatId, user, telegramUpdate.getMessage().getText());
                    }
                    break;
                }
            }
        }

        return;
    }

    private void askUsersEmail(Long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getAskUsersEmailReplyKeyboardMarkup();
        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, text,
                replyKeyboardMarkup, telegramBot, UserStatus.VerifyEmail, userRepository, telegramChatRepository);
    }

    private void checkUserEmail(Long chatId, TelegramUser user, String text) {
        if (EMailUtils.isValidEmailAddress(text)) {
            user.setStatus(UserStatus.Registered);
            user.setRegistered(true);
            user.setEmail(text);
            userRepository.save(user);

            ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
            TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, "Успешная регистрация!",
                    replyKeyboardMarkup, telegramBot, null, userRepository, telegramChatRepository);

        } else {
            askUsersEmail(chatId, "Введен некорректный email. Повторите ввод");
        }
    }

}
