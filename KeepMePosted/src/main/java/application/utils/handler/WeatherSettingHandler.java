package application.utils.handler;

import application.data.model.YandexWeather.*;
import application.data.model.service.ServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.*;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramLocationRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.service.geocoder.YandexGeoCoderService;
import application.service.weather.
        YandexWeatherService;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class WeatherSettingHandler extends AbstractTelegramHandler {
    TelegramUserRepository userRepository;
    ServiceSettingsRepository serviceSettingsRepository;
    WeatherSettingsRepository weatherSettingsRepository;
    YandexGeoCoderService yandexGeoCoderService;
    YandexWeatherService yandexWeatherService;
    TelegramLocationRepository telegramLocationRepository;

    @Override
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
                String messageToUser = saveWeatherInfoAndDoMessageToUser(user, true);

                sendMessageToUserByCustomMainKeyboard(chatId, user, messageToUser);
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
        ServiceSettings serviceSettings = saveFindServiceSettings(user, webService);

        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserIdAndCity(user.getId(), city);
        boolean needToCreateNewRule = (weatherSettings == null);
        if (needToCreateNewRule) {
            saveWeatherSettings(user, city, serviceSettings);
        }

        return needToCreateNewRule;
    }

    private ServiceSettings saveFindServiceSettings(TelegramUser user, WebService webService) {
        return serviceSettingsRepository.findByUserAndService(user, webService)
                .orElseGet(() -> {
                    ServiceSettings newServiceSettings = new ServiceSettings();
                    newServiceSettings.setService(webService);
                    newServiceSettings.setUser(user);

                    return serviceSettingsRepository.save(newServiceSettings);
                });
    }

    private void saveWeatherSettings(TelegramUser user, String city, ServiceSettings serviceSettings) {
        HashMap<String, Float> longitudeLongitude = yandexGeoCoderService.getCoordinatesByCity(city);

        WeatherSettings weatherSettings = new WeatherSettings();
        weatherSettings.setCity(city);
        weatherSettings.setUser(user);
        if (longitudeLongitude != null) {
            weatherSettings.setLatitude(longitudeLongitude.get("latitude"));
            weatherSettings.setLongitude(longitudeLongitude.get("longitude"));
        }
        weatherSettings.setServiceSettings(serviceSettings);

        weatherSettingsRepository.save(weatherSettings);
    }

    private String saveWeatherInfoAndDoMessageToUser(TelegramUser user, boolean isUserLocation) {
        String messageToUser;
        TelegramLocation userLocation = user.getLocation();
        float longitude = userLocation.getLongitude();
        float latitude = userLocation.getLatitude();

        YandexWeather weather = yandexWeatherService.getWeatherByCoordinates(Float.toString(longitude),
                Float.toString(latitude));

        if (weather == null) {
            messageToUser = "По вашему месторасположению не найдено информации о погоде!";
        } else {
            if (isUserLocation) {
                saveUserTZ(user, userLocation, weather);
            }

            YandexWeatherFact fact = weather.getFact();
            List<YandexWeatherForecast> forecast = weather.getForecasts();

            messageToUser = "Сегодня:" + "\r\n\r\n" +
                    "Сейчас " + yandexWeatherService.englishWeatherConditionToRussian(fact.getWeatherCondition()).toLowerCase()
                    + "\r\n" +
                    "Температура воздуха: " + fact.getTemp() + " ℃, по ощущениям: " + fact.getFeelsLike() + " ℃" + "\r\n" +
                    "Влажность: " + fact.getHumidity() + "%";
        }


        return messageToUser;
    }

    @Transactional
    void saveUserTZ(TelegramUser user, TelegramLocation userLocation, YandexWeather weather) {
        YandexWeatherTZInfo tzInfo = weather.getInfo().getTzInfo();
        user.setTzInfo(tzInfo);
        userLocation.setTzInfo(tzInfo);

        userRepository.save(user);
        telegramLocationRepository.save(userLocation);
    }

    //endregion

    //region Text methods

    private void textHandler(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        switch (userAnswer) {
            case TelegramBot.SETTINGS_BACK_BUTTON: {
                if (user.getStatus() == UserStatus.WeatherSettings) {
                    sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
                }
                break;
            }
            case TelegramBot.ACTIVATE_WEATHER_BUTTON: {
                saveServiceSettings(user, true);
                sendWeatherSettingsMessage(chatId, user, "Оповещения включены");
                break;
            }
            case TelegramBot.DEACTIVATE_WEATHER_BUTTON: {
                saveServiceSettings(user, false);
                sendWeatherSettingsMessage(chatId, user, "Оповещения выключены");
                break;
            }
            case TelegramBot.ADD_CITY_WEATHER_BUTTON: {
                String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                String messageToUser = listOfCities + "\r\n\r\n" + "Введите добавляемый город";
                sendTextMessageAddDeleteCity(chatId, user, messageToUser, UserStatus.AddCity);
                break;
            }
            case TelegramBot.REMOVE_CITY_WEATHER_BUTTON: {
                String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                String messageToUser = listOfCities.equals("Список отслеживаемых городов пуст") ? listOfCities :
                        listOfCities + "\r\n\r\n" + "Введите удаляемый город";

                sendTextMessageAddDeleteCity(chatId, user, messageToUser, UserStatus.RemoveCity);
                break;
            }
            case TelegramBot.LIST_FOLLOWING_CITIES_BUTTON: {
                String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                sendWeatherSettingsMessage(chatId, user, listOfCities);
                break;
            }
            case TelegramBot.CANCEL_BUTTON: {
                String messageToUser = (status == UserStatus.AddCity) ? "Добавление города отмено" :
                        "Удаление города отмено";

                sendWeatherSettingsMessage(chatId, user, messageToUser, UserStatus.WeatherSettings);
                break;
            }
            case TelegramBot.WEATHER_IN_CURRENT_LOCATION_BUTTON: {
                break;
            }
            default: {
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
                break;
            }

        }
    }

    private void saveServiceSettings(TelegramUser user, boolean active) {
        ServiceSettings serviceSettings = serviceSettingsRepository.findByUserAndService(user, WebService.YandexWeather)
                .orElseGet(() -> {
                    ServiceSettings newServiceSettings = new ServiceSettings();
                    newServiceSettings.setService(WebService.YandexWeather);
                    newServiceSettings.setUser(user);
                    return newServiceSettings;
                });

        serviceSettings.setActive(active);
        serviceSettingsRepository.save(serviceSettings);
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

    //endregion


}
