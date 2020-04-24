package application.telegram;


import application.data.model.service.ServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import application.data.repository.service.ServiceSettingsRepository;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TelegramKeyboards {

    public static ReplyKeyboardMarkup getCustomReplyMainKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        if (user.getRegistered() != null && user.getRegistered()) {
            keyboardFirstRow.add(new KeyboardButton(TelegramBot.SETTINGS_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(TelegramBot.HELP_BUTTON));

            keyboard.add(keyboardFirstRow);
        } else {
            keyboardFirstRow.add(new KeyboardButton(TelegramBot.HELLO_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(TelegramBot.REGISTER_BUTTON));

            KeyboardRow keyboardSecondRow = new KeyboardRow();
            keyboardSecondRow.add(new KeyboardButton(TelegramBot.HELP_BUTTON));

            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getAskUsersPhoneReplyKeyboardMarkup() {
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

    public static ReplyKeyboardMarkup getAskUsersEmailReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getMainSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.WEATHER_SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.NOTIFICATION_SETTINGS_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(TelegramBot.SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getWeatherSettingsKeyboard(TelegramUser user, ServiceSettingsRepository serviceSettingsRepository) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();

        ServiceSettings serviceSettings = serviceSettingsRepository.findByUserAndService(user, WebService.YandexWeather);
        if (serviceSettings != null && serviceSettings.getActive()) {
            firstKeyboardRow.add(new KeyboardButton(TelegramBot.DEACTIVATE_WEATHER_BUTTON));
        } else {
            firstKeyboardRow.add(new KeyboardButton(TelegramBot.ACTIVATE_WEATHER_BUTTON));
        }
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.SHARE_LOCATION_BUTTON).setRequestLocation(true));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.ADD_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.REMOVE_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(TelegramBot.LIST_FOLLOWING_CITIES_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(TelegramBot.SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getAddDeleteCityKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(TelegramBot.CANCEL_BUTTON));

        keyboard.add(firstKeyboardRow);

        return replyKeyboardMarkup.setKeyboard(keyboard);
    }
}
