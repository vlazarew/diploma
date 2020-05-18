package application.telegram;


import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import application.data.repository.service.NotificationServiceSettingsRepository;
import application.utils.handler.TelegramHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TelegramKeyboards {

    @Autowired
    TelegramHandler telegramHandler;

    public ReplyKeyboardMarkup getCustomReplyMainKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThirdRow = new KeyboardRow();

        if (user.getRegistered() != null && user.getRegistered()) {
            keyboardFirstRow.add(new KeyboardButton(telegramHandler.WEATHER_IN_CURRENT_LOCATION_BUTTON).setRequestLocation(true));
            keyboardFirstRow.add(new KeyboardButton(telegramHandler.SHOW_INFO_ABOUT_FOLLOWING_CITIES));

            keyboardSecondRow.add(new KeyboardButton(telegramHandler.SHOW_ALL_NEWS));
            keyboardSecondRow.add(new KeyboardButton(telegramHandler.SHOW_FOLLOWING_NEWS));

            keyboardThirdRow.add(new KeyboardButton(telegramHandler.SETTINGS_BUTTON));
        } else {
            keyboardFirstRow.add(new KeyboardButton(telegramHandler.HELLO_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(telegramHandler.REGISTER_BUTTON));

            keyboardSecondRow.add(new KeyboardButton(telegramHandler.HELP_BUTTON));
        }

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAskUsersPhoneReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.SHARE_PHONE_NUMBER).setRequestContact(true));
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.NEXT_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAskUsersEmailReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.CANCEL_REGISTRATION_BUTTON));

        keyboard.add(firstKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainSettingsKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.WEATHER_SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.NEWS_SETTINGS_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(telegramHandler.NOTIFICATION_SETTINGS_BUTTON));

        KeyboardRow fourthKeyboardRow = new KeyboardRow();
        fourthKeyboardRow.add(new KeyboardButton(telegramHandler.SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        keyboard.add(fourthKeyboardRow);

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
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.DEACTIVATE_WEATHER_BUTTON));
        } else {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.ACTIVATE_WEATHER_BUTTON));
        }
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.SHARE_LOCATION_BUTTON).setRequestLocation(true));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.ADD_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.REMOVE_CITY_WEATHER_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_CITIES_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(telegramHandler.SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getNewsSettingsKeyboard(TelegramUser user,
                                                       NotificationServiceSettingsRepository notificationServiceSettingsRepository) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();

        Optional<NotificationServiceSettings> serviceSettings = notificationServiceSettingsRepository.findByUserAndService(user, WebService.NewsService);
        if (serviceSettings.isPresent() && serviceSettings.get().getActive()) {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.DEACTIVATE_NEWS_BUTTON));
        } else {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.ACTIVATE_NEWS_BUTTON));
        }

        firstKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_CATEGORIES_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.ADD_CATEGORY_NEWS_BUTTON));
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.REMOVE_CATEGORY_NEWS_BUTTON));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(telegramHandler.SETTINGS_BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAddDeleteSomethingKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.CANCEL_BUTTON));

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
            put(900f, telegramHandler.NOTIFICATION_15_MINUTES);
            put(1800f, telegramHandler.NOTIFICATION_30_MINUTES);
            put(3600f, telegramHandler.NOTIFICATION_1_HOUR);
            put(7200f, telegramHandler.NOTIFICATION_2_HOURS);
            put(10800f, telegramHandler.NOTIFICATION_3_HOURS);
            put(21600f, telegramHandler.NOTIFICATION_6_HOURS);
            put(32400f, telegramHandler.NOTIFICATION_9_HOURS);
            put(43200f, telegramHandler.NOTIFICATION_12_HOURS);
            put(86400f, telegramHandler.NOTIFICATION_24_HOURS);
        }};

        String description = (intervalDescription.get(notificationInterval) == null) ? ""
                : intervalDescription.get(notificationInterval);

        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_15_MINUTES)));
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_30_MINUTES)));
        firstKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_1_HOUR)));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_2_HOURS)));
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_3_HOURS)));
        secondKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_6_HOURS)));

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_9_HOURS)));
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_12_HOURS)));
        thirdKeyboardRow.add(new KeyboardButton(getCorrectedDescription(description, telegramHandler.NOTIFICATION_24_HOURS)));

        KeyboardRow fourthKeyboardRow = new KeyboardRow();
        fourthKeyboardRow.add(new KeyboardButton(telegramHandler.SETTINGS_BACK_BUTTON));

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
