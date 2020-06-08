package application.telegram;


import application.data.model.service.*;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.YandexWeather.WeatherCityRepository;
import application.data.repository.service.NewsSettingsRepository;
import application.data.repository.service.NotificationServiceSettingsRepository;
import application.data.repository.service.TwitterSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
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

    //region NOT USED
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
    //endregion

    public ReplyKeyboardMarkup getCustomReplyMainKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();

        keyboardFirstRow.add(new KeyboardButton(telegramHandler.NEWS_BUTTON));
        keyboardFirstRow.add(new KeyboardButton(telegramHandler.TWITTER_BUTTON));
        keyboardFirstRow.add(new KeyboardButton(telegramHandler.WEATHER_BUTTON));

        keyboard.add(keyboardFirstRow);


        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getNewsTwitterMainPageKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.WATCH_BUTTON));
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getCommonSettingsKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.COMMON_SETTINGS_BUTTON));
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.NOTIFICATION_SETTINGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

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
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_CITIES_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

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
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_SOURCES_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getNewsWatchKeyboard(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.PREVIOUS_ITEM_BUTTON));
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.NEXT_ITEM_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.FIRST_ITEM_BUTTON));

        UserStatus status = user.getStatus();
        if (status == UserStatus.NewsWatch || status == UserStatus.NewsMainPage) {
            NewsSettingsRepository newsSettingsRepository = telegramHandler.newsSettingsRepository;
            NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());
            if (newsSettings == null) {
                newsSettings = new NewsSettings();
                newsSettings.setUser(user);
                newsSettingsRepository.save(newsSettings);
            }

            secondKeyboardRow.add(new KeyboardButton(newsSettings.isActiveUserSettings() ? telegramHandler.DEACTIVATE_PERSON_SETTINGS :
                    telegramHandler.ACTIVATE_PERSON_SETTINGS));
        } else if (status == UserStatus.TwitterWatch || status == UserStatus.TwitterMainPage) {
            TwitterSettingsRepository twitterSettingsRepository = telegramHandler.twitterSettingsRepository;
            TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
            if (twitterSettings == null) {
                twitterSettings = new TwitterSettings();
                twitterSettings.setUser(user);
                twitterSettingsRepository.save(twitterSettings);
            }

            secondKeyboardRow.add(new KeyboardButton(twitterSettings.isActiveUserSettings() ? telegramHandler.DEACTIVATE_PERSON_SETTINGS :
                    telegramHandler.ACTIVATE_PERSON_SETTINGS));
        } else if (status == UserStatus.WeatherWatch || status == UserStatus.WeatherMainPage) {
            WeatherSettingsRepository weatherSettingsRepository = telegramHandler.weatherSettingsRepository;
            WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(user.getId());
            if (weatherSettings == null) {
                weatherSettings = new WeatherSettings();
                weatherSettings.setUser(user);
                weatherSettingsRepository.save(weatherSettings);
            }

            secondKeyboardRow.add(new KeyboardButton(telegramHandler.WEATHER_IN_CURRENT_LOCATION_BUTTON).setRequestLocation(true));
        }

        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(new KeyboardButton(telegramHandler.EXIT_WATCH_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getAddDeleteCommonKeyboard(UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        if (status == UserStatus.SourcesList) {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.COMMON_BANNED_NEWS_SOURCES_ADD));
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.COMMON_BANNED_NEWS_SOURCES_DELETE));
        } else {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.COMMON_ADD));
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.COMMON_DELETE));
        }

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getTwitterSettingsKeyboard(TelegramUser user,
                                                          NotificationServiceSettingsRepository notificationServiceSettingsRepository) {

        ReplyKeyboardMarkup replyKeyboardMarkup = getTunedReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstKeyboardRow = new KeyboardRow();

        Optional<NotificationServiceSettings> serviceSettings = notificationServiceSettingsRepository.findByUserAndService(user, WebService.TwitterService);
        if (serviceSettings.isPresent() && serviceSettings.get().getActive()) {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.DEACTIVATE_TWITTER_BUTTON));
        } else {
            firstKeyboardRow.add(new KeyboardButton(telegramHandler.ACTIVATE_TWITTER_BUTTON));
        }

        firstKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_PEOPLES_BUTTON));
        firstKeyboardRow.add(new KeyboardButton(telegramHandler.LIST_FOLLOWING_HASHTAGS_BUTTON));

        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);

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

    public ReplyKeyboardMarkup getNotificationSettingsKeyboardMarkup(TelegramUser user) {
        float notificationInterval;
        HashMap<UserStatus, WebService> userStatusToWebService = new HashMap<UserStatus, WebService>() {
            {
                put(UserStatus.NewsCommonSettings, WebService.NewsService);
                put(UserStatus.TwitterCommonSettings, WebService.TwitterService);
                put(UserStatus.WeatherCommonSettings, WebService.YandexWeather);
            }
        };

        NotificationServiceSettings notificationServiceSettings = telegramHandler.saveFindServiceSettings(user,
                userStatusToWebService.get(user.getStatus()));

        notificationInterval = notificationServiceSettings.getNotificationInterval();


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
        fourthKeyboardRow.add(new KeyboardButton(telegramHandler.BACK_BUTTON));

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
        return description.equals(buttonText) ? buttonText + "✅" : buttonText;
    }

}
