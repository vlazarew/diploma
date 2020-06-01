package application.utils.handler;

import application.data.model.YandexWeather.WeatherCity;
import application.data.model.news.NewsCategory;
import application.data.model.service.NewsSettings;
import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.*;
import application.data.repository.YandexWeather.WeatherCityRepository;
import application.service.geocoder.YandexGeoCoderService;
import com.google.inject.internal.cglib.proxy.$Callback;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
@EnableAsync
public class WeatherSettingHandler extends TelegramHandler {

    @Autowired
    YandexGeoCoderService yandexGeoCoderService;

    @Autowired
    WeatherCityRepository weatherCityRepository;

    final WebService webService = WebService.YandexWeather;


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
            messageToUser = addWeatherSettingsToUser(user, city) ?
                    "Город " + city + " добавлен в список отслеживаемых" : "Город " + city + " уже отслеживается вами";
        }

        sendWeatherSettingsMessage(chatId, user, messageToUser, null);
    }

    @Transactional
    boolean addWeatherSettingsToUser(TelegramUser user, String cityText) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(user.getId());
        boolean needToCreateNewRule = (weatherSettings == null || cityText.equals(""));
        if (needToCreateNewRule) {
            saveWeatherSettings(user, cityText, notificationServiceSettings);
        } else {
            Set<WeatherCity> cities = weatherSettings.getCities();
            WeatherCity city = weatherCityRepository.findByName(cityText);
            needToCreateNewRule = (!cities.contains(city));

            if (needToCreateNewRule) {
                cities.add(city);
                weatherSettings.setCities(cities);
                weatherSettingsRepository.save(weatherSettings);
            }
        }

        return needToCreateNewRule;
    }

    private void saveWeatherSettings(TelegramUser user, String city, NotificationServiceSettings notificationServiceSettings) {
        WeatherCity weatherCity = findCreateWeatherCity(city);

        WeatherSettings weatherSettings = new WeatherSettings();
        weatherSettings.setCities(weatherCity);
        weatherSettings.setUser(user);

        weatherSettings.setNotificationServiceSettings(notificationServiceSettings);

        weatherSettingsRepository.save(weatherSettings);
    }

    private WeatherCity findCreateWeatherCity(String city) {
        WeatherCity weatherCity = weatherCityRepository.findByName(city);
        if (weatherCity == null) {
            HashMap<String, Float> longitudeLongitude = yandexGeoCoderService.getCoordinatesByCity(city);
            weatherCity = new WeatherCity();
            weatherCity.setName(city);
            if (longitudeLongitude != null) {
                weatherCity.setLatitude(longitudeLongitude.get("latitude"));
                weatherCity.setLongitude(longitudeLongitude.get("longitude"));
            }
            weatherCityRepository.save(weatherCity);
        }

        return weatherCity;
    }
    //endregion

    //region Text methods

    private void textHandler(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        boolean isWeatherStatus = (status == UserStatus.WeatherMainPage || status == UserStatus.WeatherWatch);

        if (userAnswer.equals(BACK_BUTTON)) {
            backButtonHandler(chatId, user, status);
        } else if (userAnswer.equals(WEATHER_BUTTON)) {
            sendNewsTwitterMainPageKeyboard(chatId, user, "Раздел «Яндекс.Погода»", UserStatus.WeatherMainPage);
        } else if (userAnswer.equals(ACTIVATE_NEWS_BUTTON) & status == UserStatus.WeatherSettings) {
            saveServiceSettings(user, true, webService);
            sendWeatherSettingsMessage(chatId, user, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_NEWS_BUTTON) & status == UserStatus.WeatherSettings) {
            saveServiceSettings(user, false, webService);
            sendWeatherSettingsMessage(chatId, user, "Оповещения выключены", null);
        } else if (userAnswer.equals(LIST_FOLLOWING_CITIES_BUTTON)) {
            String messageToUser = listWeatherLocationsToUser(user);
            sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.LocationList);
        } else if (status == UserStatus.LocationList) {
            locationListHandler(chatId, user, userAnswer);
        } else if (userAnswer.equals(CANCEL_BUTTON) && (status == UserStatus.AddCity || status == UserStatus.RemoveCity)) {
            String messageToUser = getMessageToUser(status);
            sendNewsSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
        } else if ((status == UserStatus.AddCity || status == UserStatus.RemoveCity)) {
            sendAddRemoveMessageToUser(chatId, user, userAnswer, status);
        }

//        if (userAnswer.equals(BACK_BUTTON) && (user.getStatus() == UserStatus.WeatherSettings)) {
////            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
//        } else if (userAnswer.equals(ACTIVATE_WEATHER_BUTTON)) {
//            saveServiceSettings(user, true, webService);
//            sendWeatherSettingsMessage(chatId, user, "Оповещения включены", null);
//        } else if (userAnswer.equals(DEACTIVATE_WEATHER_BUTTON)) {
//            saveServiceSettings(user, false, webService);
//            sendWeatherSettingsMessage(chatId, user, "Оповещения выключены", null);
//        } else if (userAnswer.equals(ADD_CITY_WEATHER_BUTTON)) {
//            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
//            String messageToUser = listOfCities + "\r\n\r\n" + "Введите добавляемый город";
//            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddCity);
//        } else if (userAnswer.equals(REMOVE_CITY_WEATHER_BUTTON)) {
//
//            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
//            String messageToUser = listOfCities.equals("Список отслеживаемых городов пуст") ? listOfCities :
//                    listOfCities + "\r\n\r\n" + "Введите удаляемый город";
//
//            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveCity);
//        } else if (userAnswer.equals(LIST_FOLLOWING_CITIES_BUTTON)) {
//            String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
//            sendWeatherSettingsMessage(chatId, user, listOfCities, null);
//        } else if (userAnswer.equals(CANCEL_BUTTON) && (status == UserStatus.AddCity || status == UserStatus.RemoveCity)) {
//            String messageToUser = (status == UserStatus.AddCity) ? "Добавление города отменено" :
//                    "Удаление города отменено";
//
//            sendWeatherSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
//        } else if (userAnswer.equals(SHOW_INFO_ABOUT_FOLLOWING_CITIES)) {
//            sendTextMessageForecastAboutFollowingCities(chatId, user, false);
//        } else if (status == UserStatus.AddCity || status == UserStatus.RemoveCity) {
//            sendAddRemoveCityMessageToUser(chatId, user, userAnswer, status);
//        }

    }

    private void sendAddRemoveMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddCity) {
            messageToUser = addWeatherSettingsToUser(user, userAnswer)
                    ? "Категория *" + userAnswer + "* добавлена в список отслеживаемых"
                    : "Категория *" + userAnswer + "* уже отслеживается вами";
        } else if (status == UserStatus.RemoveCity) {
            messageToUser = removeWeatherSettingsToUser(user, userAnswer)
                    ? "Категория *" + userAnswer + "* удалена из списка отслеживаемых"
                    : "Категория *" + userAnswer + "* не отслеживалась вами";
        } else {
            return;
        }

        sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.LocationList);
    }

    private String getMessageToUser(UserStatus status) {
        String messageToUser = "";
        if (status == UserStatus.AddCity) {
            messageToUser = "Добавление города отменено";
        } else if (status == UserStatus.RemoveCity) {
            messageToUser = "Удаление города отменено";
        }
        return messageToUser;
    }

    private void locationListHandler(Long chatId, TelegramUser user, String userAnswer) {
        if (userAnswer.equals(COMMON_ADD)) {
            String listOfCities = listWeatherLocationsToUser(user);
            String messageToUser = listOfCities + "\r\n\r\n" + "Введите добавляемый город";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddCity);
        } else if (userAnswer.equals(COMMON_DELETE)) {
            String listOfCities = listWeatherLocationsToUser(user);
            String messageToUser = listOfCities + "\r\n\r\n" + "Введите удаляемый город";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveCity);
        }
    }

    private String listWeatherLocationsToUser(TelegramUser user) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(user.getId());
        if (weatherSettings == null) {
            addWeatherSettingsToUser(user, "");
            return "*Список отслеживаемых городов пуст*";
        }

        Set<WeatherCity> weatherCities = weatherSettings.getCities();
        String headerMessage = !weatherCities.isEmpty() ? ("*Список отслеживаемых городов:* " + "\r\n\r\n")
                : "*Список отслеживаемых городов пуст*";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        weatherCities.forEach(weatherCity -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(weatherCity.getName()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    private void backButtonHandler(Long chatId, TelegramUser user, UserStatus status) {
        if (status == UserStatus.WeatherMainPage) {
            sendMessageToUserByCustomMainKeyboard(chatId, user, "Главная страница", UserStatus.MainPage);
        } else if (status == UserStatus.WeatherSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки погоды", UserStatus.WeatherCommonSettings);
        } else if (status == UserStatus.LocationList || status == UserStatus.SourcesList) {
            sendWeatherSettingsMessage(chatId, user, "Настройки рассылки погоды", UserStatus.WeatherSettings);
        }
    }

    @Transactional
    boolean removeWeatherSettingsToUser(TelegramUser user, String city) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(user.getId());

        if (weatherSettings == null) {
            return false;
        }

        Set<WeatherCity> weatherCities = weatherSettings.getCities();
        WeatherCity weatherCity = weatherCityRepository.findByName(city);

        boolean needToDelete = (weatherCities.contains(weatherCity));
        if (needToDelete) {
            weatherCities.remove(weatherCity);
            weatherSettings.setCities(weatherCities);
            weatherSettingsRepository.save(weatherSettings);
        }

        return needToDelete;
    }
    //endregion


}
