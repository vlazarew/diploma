package application.utils.handler;

import application.data.model.YandexWeather.WeatherCity;
import application.data.model.news.NewsCategory;
import application.data.model.service.*;
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

import java.util.*;
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
            case WeatherWatch: {
//                sendTextMessageForecastAboutFollowingCities(chatId, user, true);
                sendWeatherWatchKeyboard(chatId, user, UserStatus.WeatherWatch, true, true);
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
            WeatherCity city = findCreateWeatherCity(cityText);
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
        Set<WeatherCity> weatherCities = new HashSet<>();
        weatherCities.add(weatherCity);
        weatherSettings.setCities(weatherCities);
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
            sendWeatherSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
        } else if ((status == UserStatus.AddCity || status == UserStatus.RemoveCity)) {
            sendAddRemoveMessageToUser(chatId, user, userAnswer, status);
        } else if (((userAnswer.equals(WATCH_BUTTON) || userAnswer.equals(NEXT_ITEM_BUTTON)) & isWeatherStatus)) {
            sendWeatherWatchKeyboard(chatId, user, UserStatus.WeatherWatch, true, false);
        } else if (userAnswer.equals(PREVIOUS_ITEM_BUTTON) & isWeatherStatus) {
            sendWeatherWatchKeyboard(chatId, user, UserStatus.WeatherWatch, false, false);
        } else if (userAnswer.equals(EXIT_WATCH_BUTTON) & isWeatherStatus) {
            sendNewsTwitterMainPageKeyboard(chatId, user, "Раздел Яндекс.Погода", UserStatus.WeatherMainPage);
        } else if (userAnswer.equals(FIRST_ITEM_BUTTON) & isWeatherStatus) {
            clearUserWeatherHistory(user, chatId);
//        } else if (userAnswer.equals(WEATHER_IN_CURRENT_LOCATION_BUTTON)) {
//            sendWeatherWatchKeyboard(chatId, user, UserStatus.WeatherWatch, true, true);
        }
    }

    private void clearUserWeatherHistory(TelegramUser telegramUser, Long chatId) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(telegramUser.getId());
        if (weatherSettings == null) {
            return;
        }

        weatherSettings.setLastViewedWeatherCity(null);
        weatherSettings.setLastCityCreationDate(new Date());
        weatherSettings.setViewedCities(new HashSet<>());
        weatherSettingsRepository.save(weatherSettings);

        sendWeatherWatchKeyboard(chatId, telegramUser, UserStatus.WeatherWatch, true, false);
    }

    private void sendAddRemoveMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddCity) {
            messageToUser = addWeatherSettingsToUser(user, userAnswer)
                    ? "Город *" + userAnswer + "* добавлен в список отслеживаемых"
                    : "Город *" + userAnswer + "* уже отслеживается вами";
        } else if (status == UserStatus.RemoveCity) {
            messageToUser = removeWeatherSettingsToUser(user, userAnswer)
                    ? "Город *" + userAnswer + "* удален из списка отслеживаемых"
                    : "Город *" + userAnswer + "* не отслеживается вами";
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
//            addWeatherSettingsToUser(user, "");
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
