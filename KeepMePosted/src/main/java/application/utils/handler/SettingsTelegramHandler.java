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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class SettingsTelegramHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    UserRepository userRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isText){
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer){
                case TelegramBot.SETTINGS_BUTTON:{
                    sendSettingsKeyboard(chatId, user);
                    break;
                }
                default:{
                    break;
                }
            }
        }

    }

    private void sendSettingsKeyboard(Long chatId, TelegramUser user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Поделитесь номером телефона (опционально)");

        sendMessage.setReplyMarkup(createSettingsKeyboard());

        try {
            telegramBot.execute(sendMessage);

            user.setStatus(UserStatus.VerifyPhone);
            userRepository.save(user);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup createSettingsKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.WEATHER_SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.NOTIFICATION_SETTINGS_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}
