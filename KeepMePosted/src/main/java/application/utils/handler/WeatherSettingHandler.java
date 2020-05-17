package application.utils.handler;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.*;
import application.service.geocoder.YandexGeoCoderService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
@EnableAsync
public class WeatherSettingHandler extends TelegramHandler {
    YandexGeoCoderService yandexGeoCoderService;

    public WeatherSettingHandler(YandexGeoCoderService yandexGeoCoderService) {
        this.yandexGeoCoderService = yandexGeoCoderService;
    }

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser user = telegramMessage.getFrom();
        UserStatus status = user.getStatus();

        if (hasLocation) {
            locationHandler(chatId, user, status);
            return;
        }

        if (!hasText) {
            return;
        }

        String userAnswer = telegramMessage.getText();
        textHandler(chatId, user, userAnswer, status);
    }

    // region Location methods

    private void locationHandler(Long chatId, TelegramUser user, UserStatus status) {
        switch (status) {
            case WeatherSettings: {
                addCityToFollow(chatId, user);
                break;
            }
            case MainPage: {
                sendTextMessageForecastAboutFollowingCities(chatId, user, true);
                break;
            }
        }
    }

    private void addCityToFollow(Long chatId, TelegramUser user) {
        String messageToUser;
        String city = user.getLocation().getCity();

        if (city == null) {
            messageToUser = "По вашей геопозиции не найден город. Добавьте его вручную";
        } else {
            messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, city) ?
                    "Город " + city + " добавлен в список отслеживаемых" : "Город " + city + " уже отслеживается вами";
        }

        sendWeatherSettingsMessage(chatId, user, messageToUser, null);
    }

    @Transactional
    boolean addWeatherSettingsToUser(TelegramUser user, WebService webService, String city) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserIdAndCity(user.getId(), city);
        boolean needToCreateNewRule = (weatherSettings == null);
        if (needToCreateNewRule) {
            saveWeatherSettings(user, city, notificationServiceSettings);
        }

        return needToCreateNewRule;
    }

    private NotificationServiceSettings saveFindServiceSettings(TelegramUser user, WebService webService) {
        return notificationServiceSettingsRepository.findByUserAndService(user, webService)
                .orElseGet(() -> {
                    NotificationServiceSettings newNotificationServiceSettings = new NotificationServiceSettings();
                    newNotificationServiceSettings.setService(webService);
                    newNotificationServiceSettings.setUser(user);

                    return notificationServiceSettingsRepository.save(newNotificationServiceSettings);
                });
    }

    private void saveWeatherSettings(TelegramUser user, String city, NotificationServiceSettings notificationServiceSettings) {
        HashMap<String, Float> longitudeLongitude = yandexGeoCoderService.getCoordinatesByCity(city);

        WeatherSettings weatherSettings = new WeatherSettings();
        weatherSettings.setCity(city);
        weatherSettings.setUser(user);
        if (longitudeLongitude != null) {
            weatherSettings.setLatitude(longitudeLongitude.get("latitude"));
            weatherSettings.setLongitude(longitudeLongitude.get("longitude"));
        }
        weatherSettings.setNotificationServiceSettings(notificationServiceSettings);

        weatherSettingsRepository.save(weatherSettings);
    }
    //endregion

    //region Text methods

    private void textHandler(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        if (userAnswer.equals(SETTINGS_BACK_BUTTON) && (user.getStatus() == UserStatus.WeatherSettings)) {
            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
        } else if (userAnswer.equals(ACTIVATE_WEATHER_BUTTON)) {
            saveServiceSettings(user, true);
            sendWeatherSettingsMessage(chatId, user, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_WEATHER_BUTTON)) {
            saveServiceSettings(user, false);
            sendWeatherSettingsMessage(chatId, user, "Оповещения выключены", null);
        } else if (userAnswer.equals(ADD_CITY_WEATHER_BUTTON)) {
            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
            String messageToUser = listOfCities + "\r\n\r\n" + "Введите добавляемый город";
            sendTextMessageAddDeleteCity(chatId, messageToUser, UserStatus.AddCity);
        } else if (userAnswer.equals(REMOVE_CITY_WEATHER_BUTTON)) {

            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
            String messageToUser = listOfCities.equals("Список отслеживаемых городов пуст") ? listOfCities :
                    listOfCities + "\r\n\r\n" + "Введите удаляемый город";

            sendTextMessageAddDeleteCity(chatId, messageToUser, UserStatus.RemoveCity);
        } else if (userAnswer.equals(LIST_FOLLOWING_CITIES_BUTTON)) {
            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
            sendWeatherSettingsMessage(chatId, user, listOfCities, null);
        } else if (userAnswer.equals(CANCEL_BUTTON)) {
            String messageToUser = (status == UserStatus.AddCity) ? "Добавление города отмено" :
                    "Удаление города отмено";

            sendWeatherSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
        } else if (userAnswer.equals(SHOW_INFO_ABOUT_FOLLOWING_CITIES)) {
            sendTextMessageForecastAboutFollowingCities(chatId, user, false);
        } else if (status == UserStatus.AddCity || status == UserStatus.RemoveCity) {
            sendAddRemoveCityMessageToUser(chatId, user, userAnswer, status);
        }

    }

    private void sendAddRemoveCityMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddCity) {
            messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, userAnswer)
                    ? "Город " + userAnswer + " добавлен в список отслеживаемых"
                    : "Город " + userAnswer + " уже отслеживается вами";
        } else if (status == UserStatus.RemoveCity) {
            messageToUser = removeWeatherSettingsToUser(user, WebService.YandexWeather, userAnswer)
                    ? "Город " + userAnswer + " удален из списка отслеживаемых"
                    : "Город " + userAnswer + " не отслеживался вами";
        } else {
            return;
        }

        sendWeatherSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
    }

    private void saveServiceSettings(TelegramUser user, boolean active) {
        NotificationServiceSettings notificationServiceSettings = notificationServiceSettingsRepository.findByUserAndService(user, WebService.YandexWeather)
                .orElseGet(() -> {
                    NotificationServiceSettings newNotificationServiceSettings = new NotificationServiceSettings();
                    newNotificationServiceSettings.setService(WebService.YandexWeather);
                    newNotificationServiceSettings.setUser(user);
                    return newNotificationServiceSettings;
                });

        notificationServiceSettings.setActive(active);
        notificationServiceSettingsRepository.save(notificationServiceSettings);
    }

    private String listWeatherSettingToUser(TelegramUser user, WebService webService) {
        List<WeatherSettings> weatherSettings = weatherSettingsRepository.findByUserId(user.getId());
        String headerMessage = weatherSettings.size() > 0 ? ("Список отслеживаемых городов: " + "\r\n\r\n")
                : "Список отслеживаемых городов пуст";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        weatherSettings.forEach(weatherSetting -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(weatherSetting.getCity()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    @Transactional
    boolean removeWeatherSettingsToUser(TelegramUser user, WebService webService, String city) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserIdAndCity(user.getId(), city);
        boolean needToDelete = (weatherSettings != null);
        if (needToDelete) {
            weatherSettingsRepository.delete(weatherSettings);
        }

        return needToDelete;
    }


    private void sendTextMessageAddDeleteCity(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAddDeleteCityKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    //endregion


}
