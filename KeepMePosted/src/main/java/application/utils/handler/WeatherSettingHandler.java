package application.utils.handler;

import application.data.model.YandexWeather.*;
import application.data.model.service.ServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramLocation;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramLocationRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.service.geocoder.YandexGeoCoder;
import application.service.weather.
        YandexWeatherService;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import application.telegram.TelegramSendMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class WeatherSettingHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    TelegramUserRepository userRepository;
    ServiceSettingsRepository serviceSettingsRepository;
    WeatherSettingsRepository weatherSettingsRepository;
    SettingsTelegramHandler settingsTelegramHandler;
    TelegramChatRepository telegramChatRepository;
    YandexWeatherService yandexWeatherService;
    TelegramLocationRepository telegramLocationRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isLocation) {
            switch (status) {
                case WeatherSettings: {
                    if (user.getLocation().getCity() == null) {
                        sendWeatherSettingsMessage(user, chatId, "По вашей геопозиции не найден город. Добавьте его вручную", null);
                        return;
                    }

                    String city = user.getLocation().getCity();

                    String messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, city) ?
                            "Город " + city + " добавлен в список отслеживаемых" : "Город " + city + " уже отслеживается вами";

                    sendWeatherSettingsMessage(user, chatId, messageToUser, null);
                    break;
                }
                case MainPage: {

                    String messageToUser = saveWeatherInfoAndDoMessageToUser(user, true);

                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, messageToUser, replyKeyboardMarkup,
                            telegramBot, null, null, null);

                    break;
                }
            }
        } else if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.SETTINGS_BACK_BUTTON: {
                    if (user.getStatus() == UserStatus.WeatherSettings) {
                        TelegramSendMessage.sendSettingsKeyboard(chatId, "Настройки бота", telegramBot,
                                UserStatus.Settings, userRepository, telegramChatRepository);
                    }
                    break;
                }
                case TelegramBot.ACTIVATE_WEATHER_BUTTON: {
                    ServiceSettings serviceSettings = serviceSettingsRepository.findByUserAndService(user, WebService.YandexWeather);

                    if (serviceSettings == null) {
                        serviceSettings = ServiceSettings.builder()
                                .service(WebService.YandexWeather)
                                .user(user)
                                .build();
                    }

                    serviceSettings.setActive(true);
                    serviceSettingsRepository.save(serviceSettings);

                    sendWeatherSettingsMessage(user, chatId, "Оповещения включены", null);
                    break;
                }
                case TelegramBot.DEACTIVATE_WEATHER_BUTTON: {
                    ServiceSettings serviceSettings = serviceSettingsRepository.findByUserAndService(user, WebService.YandexWeather);

                    if (serviceSettings == null) {
                        serviceSettings = ServiceSettings.builder()
                                .service(WebService.YandexWeather)
                                .user(user)
                                .build();
                    }

                    serviceSettings.setActive(false);
                    serviceSettingsRepository.save(serviceSettings);

                    sendWeatherSettingsMessage(user, chatId, "Оповещения выключены", null);
                    break;
                }
                case TelegramBot.ADD_CITY_WEATHER_BUTTON: {
                    String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getAddDeleteCityKeyboardMarkup();
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, listOfCities + "\r\n\r\n\r\n" +
                                    "Введите добавляемый город",
                            replyKeyboardMarkup, telegramBot, UserStatus.AddCity, userRepository, telegramChatRepository);
                    break;
                }
                case TelegramBot.REMOVE_CITY_WEATHER_BUTTON: {
                    String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                    String userMessage = listOfCities.equals("Список отслеживаемых городов пуст") ? listOfCities :
                            listOfCities + "\r\n\r\n\r\n" + "Введите удаляемый город";

                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getAddDeleteCityKeyboardMarkup();
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, userMessage,
                            replyKeyboardMarkup, telegramBot, UserStatus.RemoveCity, userRepository, telegramChatRepository);
                    break;
                }
                case TelegramBot.LIST_FOLLOWING_CITIES_BUTTON: {
                    String listOfCities = listWeatherSettingToUser(user, WebService.YandexWeather);
                    sendWeatherSettingsMessage(user, chatId, listOfCities, null);
                    break;
                }
                case TelegramBot.CANCEL_BUTTON: {
                    if (status == UserStatus.AddCity) {
                        sendWeatherSettingsMessage(user, chatId, "Добавление города отмено", UserStatus.WeatherSettings);
                    }
                    if (status == UserStatus.RemoveCity) {
                        sendWeatherSettingsMessage(user, chatId, "Удаление города отмено", UserStatus.WeatherSettings);
                    }
                    break;
                }
                case TelegramBot.WEATHER_IN_CURRENT_LOCATION_BUTTON: {
                    break;
                }
                default: {
                    if (status == UserStatus.AddCity) {
                        String messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, userAnswer) ?
                                "Город " + userAnswer + " добавлен в список отслеживаемых" : "Город " + userAnswer + " уже отслеживается вами";

                        sendWeatherSettingsMessage(user, chatId, messageToUser, UserStatus.WeatherSettings);
                    }
                    if (status == UserStatus.RemoveCity) {
                        String messageToUser = removeWeatherSettingsToUser(user, WebService.YandexWeather, userAnswer) ?
                                "Город " + userAnswer + " удален из списка отслеживаемых" : "Город " + userAnswer + " не отслеживался вами";

                        sendWeatherSettingsMessage(user, chatId, messageToUser, UserStatus.WeatherSettings);
                    }
                    break;
                }
            }
        }
    }

    private void sendWeatherSettingsMessage(TelegramUser user, Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getWeatherSettingsKeyboard(user, serviceSettingsRepository);
        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, text,
                replyKeyboardMarkup, telegramBot, status, userRepository, telegramChatRepository);
    }

    @Transactional
    boolean addWeatherSettingsToUser(TelegramUser user, WebService webService, String city) {

        ServiceSettings serviceSettings = serviceSettingsRepository.findByUserAndService(user, webService);
        if (serviceSettings == null) {

            serviceSettings = new ServiceSettings();
            serviceSettings.setService(webService);
            serviceSettings.setUser(user);

            serviceSettingsRepository.save(serviceSettings);
        }

        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserIdAndCity(user.getId(), city);
        boolean needToCreateNewRule = (weatherSettings == null);
        if (needToCreateNewRule) {

            HashMap<String, Float> longitudeLongitude = YandexGeoCoder.getCoordinatesByCity(city);

            weatherSettings = new WeatherSettings();
            weatherSettings.setCity(city);
            weatherSettings.setUser(user);
            if (longitudeLongitude != null) {
                weatherSettings.setLatitude(longitudeLongitude.get("latitude"));
                weatherSettings.setLongitude(longitudeLongitude.get("longitude"));
            }
            weatherSettings.setServiceSettings(serviceSettings);

            weatherSettingsRepository.save(weatherSettings);
        }

        return needToCreateNewRule;
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

    private String listWeatherSettingToUser(TelegramUser user, WebService webService) {
        List<WeatherSettings> weatherSettings = weatherSettingsRepository.findByUserId(user.getId());
        String headerMessage = weatherSettings.size() > 0 ? ("Список отслеживаемых городов: " + "\r\n\r\n")
                : "Список отслеживаемых городов пуст";
        StringBuilder stringBuilder = new StringBuilder(headerMessage);
        AtomicInteger count = new AtomicInteger();

        weatherSettings.forEach(weatherSetting -> {
            count.getAndIncrement();

            stringBuilder.append(count).append(". ").append(weatherSetting.getCity());
        });

        return stringBuilder.toString();
    }

    @Transactional
    String saveWeatherInfoAndDoMessageToUser(TelegramUser user, boolean isUserLocation) {
        float longitude = user.getLocation().getLongitude();
        float latitude = user.getLocation().getLatitude();

        YandexWeather weather = yandexWeatherService.getWeatherByCoordinates(Float.toString(longitude), Float.toString(latitude));
        String messageToUser;

        if (weather == null) {
            messageToUser = "По вашему месторасположению не найдено информации о погоде!";
        } else {
            if (isUserLocation) {
                YandexWeatherTZInfo tzInfo = weather.getInfo().getTzInfo();
                user.setTzInfo(tzInfo);
                TelegramLocation location = user.getLocation();
                location.setTzInfo(tzInfo);

                userRepository.save(user);
                telegramLocationRepository.save(location);
            }

            YandexWeatherFact fact = weather.getFact();
            Set<YandexWeatherForecast> forecast = weather.getForecasts();

            messageToUser = "Сегодня:" + "\r\n\r\n" +
                    "Сейчас " + YandexWeatherService.englishWeatherConditionToRussian(fact.getWeatherCondition()).toLowerCase()
                    + "\r\n" +
                    "Температура воздуха: " + fact.getTemp() + " ℃, по ощущениям: " + fact.getFeelsLike() + " ℃" + "\r\n" +
                    "Влажность: " + fact.getHumidity() + "%";
        }


        return messageToUser;
    }
}
