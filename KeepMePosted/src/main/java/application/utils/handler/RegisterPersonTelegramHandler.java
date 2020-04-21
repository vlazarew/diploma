package application.utils.handler;

import application.data.model.TelegramUpdate;
import application.data.model.TelegramUser;
import application.data.model.UserStatus;
import application.data.repository.UserRepository;
import application.telegram.TelegramBot;
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
    UserRepository userRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isContact) {
            askUsersEmail(chatId, user, "Введите свой e-mail адрес (обязательно)");
        } else if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.REGISTER_BUTTON: {
                    askUsersPhone(chatId, user);
                    break;
                }
                case TelegramBot.NEXT_BUTTON: {
                    askUsersEmail(chatId, user, "Введите свой e-mail адрес (обязательно)");
                    break;
                }
                case TelegramBot.CANCEL_REGISTRATION_BUTTON: {
                    telegramBot.sendTextMessage(chatId, "Регистрация отменена");
                    break;
                }
//                case TelegramBot.CONFIRM_EMAIL: {
//                    checkUserEmail(chatId, user, telegramUpdate.getMessage().getText());
//                }
                default: {
                    if (status==UserStatus.VerifyEmail){
                        checkUserEmail(chatId, user, telegramUpdate.getMessage().getText());
                    }
                    break;
                }
            }
        }

        return;
    }

    private void askUsersPhone(Long chatId, TelegramUser user) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Поделитесь номером телефона (опционально)");

        sendMessage.setReplyMarkup(askUsersPhoneReplyKeyboardMarkup());

        try {
            telegramBot.execute(sendMessage);

            user.setStatus(UserStatus.VerifyPhone);
            userRepository.save(user);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup askUsersPhoneReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.SHARE_PHONE_NUMBER).setRequestContact(true));
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.NEXT_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private void askUsersEmail(Long chatId, TelegramUser user, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(askUsersEmailReplyKeyboardMarkup());

        try {
            telegramBot.execute(sendMessage);

            user.setStatus(UserStatus.VerifyEmail);
            userRepository.save(user);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup askUsersEmailReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
//        KeyboardRow firstKeyboardRow = new KeyboardRow();
//        firstKeyboardRow.add(new KeyboardButton(TelegramBot.CONFIRM_EMAIL));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.CANCEL_REGISTRATION_BUTTON));

//        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private void checkUserEmail(Long chatId, TelegramUser user, String text) {
        if (EMailUtils.isValidEmailAddress(text)) {
            user.setStatus(UserStatus.Registered);
            user.setRegistered(true);
            user.setEmail(text);
            userRepository.save(user);

            telegramBot.sendTextMessage(chatId, "Успешная регистрация!");
        } else {
            askUsersEmail(chatId, user, "Введен некорректный email. Повторите ввод");
        }
    }

}
