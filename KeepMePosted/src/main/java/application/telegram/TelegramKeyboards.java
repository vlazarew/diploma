package application.telegram;


import application.data.model.service.ServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import application.data.repository.service.ServiceSettingsRepository;
import application.utils.handler.AbstractTelegramHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TelegramKeyboards extends AbstractTelegramHandler{

    public ReplyKeyboardMarkup getCustomReplyMainKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        if (user.getRegistered() != null && user.getRegistered()) {
            keyboardFirstRow.add(new KeyboardButton(SETTINGS_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(WEATHER_IN_CURRENT_LOCATION_BUTTON)
                    .setRequestLocation(true));

            keyboard.add(keyboardFirstRow);
        } else {
            keyboardFirstRow.add(new KeyboardButton(HELLO_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(REGISTER_BUTTON));

            KeyboardRow keyboardSecondRow = new KeyboardRow();
            keyboardSecondRow.add(new KeyboardButton(HELP_BUTTON));

            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAskUsersPhoneReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(SHARE_PHONE_NUMBER).setRequestContact(true));
        firstKeyboardRow.add(new KeyboardButton(NEXT_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAskUsersEmailReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(WEATHER_SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(NOTIFICATION_SETTINGS_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getWeatherSettingsKeyboard(TelegramUser user, ServiceSettingsRepository serviceSettingsRepository) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();

        Optional<ServiceSettings> serviceSettings = serviceSettingsRepository.findByUserAndService(user, WebService.YandexWeather);
        if (serviceSettings.isPresent() && serviceSettings.get().getActive()) {
            firstKeyboardRow.add(new KeyboardButton(DEACTIVATE_WEATHER_BUTTON));
        } else {
            firstKeyboardRow.add(new KeyboardButton(ACTIVATE_WEATHER_BUTTON));
        }
        firstKeyboardRow.add(new KeyboardButton(SHARE_LOCATION_BUTTON).setRequestLocation(true));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(ADD_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(REMOVE_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(LIST_FOLLOWING_CITIES_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAddDeleteCityKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(CANCEL_BUTTON));

        keyboard.add(firstKeyboardRow);

        return replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private ReplyKeyboardMarkup getTunedReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }
}
