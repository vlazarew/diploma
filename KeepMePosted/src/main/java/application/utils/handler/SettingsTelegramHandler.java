package application.utils.handler;

import application.data.model.TelegramUpdate;
import application.data.model.TelegramUser;
import application.data.model.UserStatus;
import application.data.repository.TelegramUserRepository;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class SettingsTelegramHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    TelegramUserRepository userRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.SETTINGS_BUTTON: {
                    sendSettingsKeyboard(chatId, user, "Настройки бота");
                    break;
                }
                case TelegramBot.NOTIFICATION_SETTINGS_BUTTON: {
                    sendSettingsKeyboard(chatId, user, "Настройка графика оповещений в разработке");
                    break;
                }
                case TelegramBot.WEATHER_SETTINGS_BUTTON: {
                    sendWeatherSettingKeyboard(chatId, user, "Настройки рассылки погоды");
                    break;
                }
                default: {
                    break;
                }
            }
        } else if (isLocation) {
            int s = 1;
        }

    }

    private void sendSettingsKeyboard(Long chatId, TelegramUser user, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(createSettingsKeyboard());

        try {
            telegramBot.execute(sendMessage);

//            user.setStatus(UserStatus.VerifyPhone);
//            userRepository.save(user);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup createSettingsKeyboard() {
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

    private void sendWeatherSettingKeyboard(Long chatId, TelegramUser user, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(createWeatherSettingsKeyboard());

        try {
            telegramBot.execute(sendMessage);

//            user.setStatus(UserStatus.VerifyPhone);
//            userRepository.save(user);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup createWeatherSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        // if bla-bla
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.ACTIVATE_WEATHER_BUTTON));
        // else
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.DEACTIVATE_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.SHARE_LOCATION_BUTTON).setRequestLocation(true));
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.ADD_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.REMOVE_CITY_WEATHER_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }


}
