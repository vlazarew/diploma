package application.telegram;


import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import application.data.repository.service.NotificationServiceSettingsRepository;
import application.notification.NotificationService;
import application.utils.handler.AbstractTelegramHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TelegramKeyboards extends AbstractTelegramHandler {

    public ReplyKeyboardMarkup getCustomReplyMainKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        if (user.getRegistered() != null && user.getRegistered()) {
            keyboardFirstRow.add(new KeyboardButton(WEATHER_IN_CURRENT_LOCATION_BUTTON).setRequestLocation(true));
            keyboardFirstRow.add(new KeyboardButton(SHOW_INFO_ABOUT_FOLLOWING_CITIES));

            keyboardSecondRow.add(new KeyboardButton(SETTINGS_BUTTON));
        } else {
            keyboardFirstRow.add(new KeyboardButton(HELLO_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(REGISTER_BUTTON));

            keyboardSecondRow.add(new KeyboardButton(HELP_BUTTON));
        }

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

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

    public ReplyKeyboardMarkup getWeatherSettingsKeyboard(TelegramUser user,
                                                          NotificationServiceSettingsRepository notificationServiceSettingsRepository) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();

        Optional<NotificationServiceSettings> serviceSettings = notificationServiceSettingsRepository.findByUserAndService(user, WebService.YandexWeather);
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

    public ReplyKeyboardMarkup getNotificationSettingsKeyboardMarkup(TelegramUser user,
                                                                     NotificationServiceSettingsRepository notificationServiceSettingsRepository) {
        float notificationInterval = 0;
        List<NotificationServiceSettings> notificationServiceSettingsList = notificationServiceSettingsRepository.findByUser(user);
        if (notificationServiceSettingsList.size() > 0) {
            notificationInterval = notificationServiceSettingsList.get(0).getNotificationInterval();
        }

        HashMap<Float, String> intervalDescription = new HashMap<Float, String>() {{
            put(900f, NOTIFICATION_15_MINUTES);
            put(1800f, NOTIFICATION_30_MINUTES);
            put(3600f, NOTIFICATION_1_HOUR);
            put(7200f, NOTIFICATION_2_HOURS);
            put(10800f, NOTIFICATION_3_HOURS);
            put(21600f, NOTIFICATION_6_HOURS);
            put(32400f, NOTIFICATION_9_HOURS);
            put(43200f, NOTIFICATION_12_HOURS);
            put(86400f, NOTIFICATION_24_HOURS);
        }};

        String description = (intervalDescription.get(notificationInterval) == null) ? ""
                : intervalDescription.get(notificationInterval);

        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_15_MINUTES)));
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_30_MINUTES)));
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_1_HOUR)));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_2_HOURS)));
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_3_HOURS)));
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_6_HOURS)));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_9_HOURS)));
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_12_HOURS)));
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, NOTIFICATION_24_HOURS)));

        KeyboardRow fourthKeyboardRow = new KeyboardRow();
        fourthKeyboardRow.add(new KeyboardButton(SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboard.add(fourthKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getTunedReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    private String getCorrectedDescription(String description, String buttonText) {
        return description.equals(buttonText) ? buttonText + " (выбрано)" : buttonText;
    }

}
