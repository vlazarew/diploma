package application.utils.handler;

import application.data.model.YandexWeather.YandexWeather;
import application.data.model.YandexWeather.YandexWeatherFact;
import application.data.model.YandexWeather.YandexWeatherTZInfo;
import application.data.model.service.WeatherSettings;
import application.data.model.telegram.*;
import application.data.repository.service.NotificationServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramLocationRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.service.weather.YandexWeatherService;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
@PropertySource("classpath:interface.properties")
@EnableAsync
public abstract class AbstractTelegramHandler implements TelegramMessageHandler {

    @Autowired
    TelegramBot telegramBot;
    @Autowired
    public TelegramUserRepository userRepository;
    @Autowired
    public TelegramChatRepository telegramChatRepository;
    @Autowired
    public NotificationServiceSettingsRepository notificationServiceSettingsRepository;
    @Autowired
    public TelegramKeyboards telegramKeyboards;
    @Autowired
    public WeatherSettingsRepository weatherSettingsRepository;
    @Autowired
    public YandexWeatherService yandexWeatherService;
    @Autowired
    public TelegramLocationRepository telegramLocationRepository;

    //region Кнопки в приложении
    @Value("${telegram.START_COMMAND}")
    public String START_COMMAND;

    // Базовые кнопки
    @Value("${telegram.HELLO_BUTTON}")
    public String HELLO_BUTTON;
    @Value("${telegram.HELP_BUTTON}")
    public String HELP_BUTTON;

    // Кнопки регистрации
    @Value("${telegram.REGISTER_BUTTON}")
    public String REGISTER_BUTTON;
    @Value("${telegram.CANCEL_REGISTRATION_BUTTON}")
    public String CANCEL_REGISTRATION_BUTTON;
    @Value("${telegram.NEXT_BUTTON}")
    public String NEXT_BUTTON;
    @Value("${telegram.SHARE_PHONE_NUMBER}")
    public String SHARE_PHONE_NUMBER;
    @Value("${telegram.CONFIRM_EMAIL}")
    public String CONFIRM_EMAIL;

    // Кнопки настроек
    @Value("${telegram.SETTINGS_BUTTON}")
    public String SETTINGS_BUTTON;
    @Value("${telegram.NOTIFICATION_SETTINGS_BUTTON}")
    public String NOTIFICATION_SETTINGS_BUTTON;
    @Value("${telegram.SETTINGS_BACK_BUTTON}")
    public String SETTINGS_BACK_BUTTON;

    // Интеграция с погодой
    @Value("${telegram.WEATHER_SETTINGS_BUTTON}")
    public String WEATHER_SETTINGS_BUTTON;
    @Value("${telegram.ACTIVATE_WEATHER_BUTTON}")
    public String ACTIVATE_WEATHER_BUTTON;
    @Value("${telegram.DEACTIVATE_WEATHER_BUTTON}")
    public String DEACTIVATE_WEATHER_BUTTON;
    @Value("${telegram.SHARE_LOCATION_BUTTON}")
    public String SHARE_LOCATION_BUTTON;
    @Value("${telegram.ADD_CITY_WEATHER_BUTTON}")
    public String ADD_CITY_WEATHER_BUTTON;
    @Value("${telegram.REMOVE_CITY_WEATHER_BUTTON}")
    public String REMOVE_CITY_WEATHER_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_CITIES_BUTTON}")
    public String LIST_FOLLOWING_CITIES_BUTTON;
    @Value("${telegram.CANCEL_BUTTON}")
    public String CANCEL_BUTTON;
    @Value("${telegram.WEATHER_IN_CURRENT_LOCATION_BUTTON}")
    public String WEATHER_IN_CURRENT_LOCATION_BUTTON;
    @Value("${telegram.SHOW_INFO_ABOUT_FOLLOWING_CITIES}")
    public String SHOW_INFO_ABOUT_FOLLOWING_CITIES;

    //region Интервалы оповещений
    @Value("${telegram.NOTIFICATION_15_MINUTES}")
    public String NOTIFICATION_15_MINUTES;
    @Value("${telegram.NOTIFICATION_30_MINUTES}")
    public String NOTIFICATION_30_MINUTES;
    @Value("${telegram.NOTIFICATION_1_HOUR}")
    public String NOTIFICATION_1_HOUR;
    @Value("${telegram.NOTIFICATION_2_HOURS}")
    public String NOTIFICATION_2_HOURS;
    @Value("${telegram.NOTIFICATION_3_HOURS}")
    public String NOTIFICATION_3_HOURS;
    @Value("${telegram.NOTIFICATION_6_HOURS}")
    public String NOTIFICATION_6_HOURS;
    @Value("${telegram.NOTIFICATION_9_HOURS}")
    public String NOTIFICATION_9_HOURS;
    @Value("${telegram.NOTIFICATION_12_HOURS}")
    public String NOTIFICATION_12_HOURS;
    @Value("${telegram.NOTIFICATION_24_HOURS}")
    public String NOTIFICATION_24_HOURS;
    //endregion
    //endregion

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
    }

    @Override
    public void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendSettingsKeyboard(Long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getMainSettingsKeyboard();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendSettingsKeyboard(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getMainSettingsKeyboard();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageReplyKeyboardMarkup(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup,
                                                   UserStatus status) {
        SendMessage sendMessage = makeSendMessage(chatId, text, replyKeyboardMarkup);

        try {
            telegramBot.execute(sendMessage);

            if (status != null) {
                TelegramUser user;
                Optional<TelegramChat> chat = telegramChatRepository.findById(chatId);

                if (chat.isPresent()) {
                    user = chat.get().getUser();

                    user.setStatus(status);
                    userRepository.save(user);
                } else {
                    log.error("Не найден чат с id: " + chatId);
                }
            }

        } catch (TelegramApiException e) {
            log.error(e);
        }

    }

    private SendMessage makeSendMessage(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    @Override
    public void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getWeatherSettingsKeyboard(user,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getWeatherSettingsKeyboard(user,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageAddDeleteCity(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAddDeleteCityKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
//    @Async
    public void sendTextMessageForecastAboutFollowingCities(Long chatId, TelegramUser telegramUser, boolean isUserLocation) {
        List<String> textList = getInfoAboutFollowingCities(telegramUser, isUserLocation);
//        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);

        textList.forEach(text -> sendTextMessageReplyKeyboardMarkup(chatId, text, null, null));
    }


    private List<String> getInfoAboutFollowingCities(TelegramUser telegramUser, boolean isUserLocation) {
        List<WeatherSettings> weatherSettingsList = weatherSettingsRepository.findByUserId(telegramUser.getId());
        List<String> resultList = new ArrayList<>();

        if (weatherSettingsList.size() == 0) {
            resultList.add("Список отслеживаемых городов пуст.");
        } else if (isUserLocation) {
            TelegramLocation userLocation = telegramUser.getLocation();
            String infoAboutCity = saveWeatherInfoAndDoMessageToUser(userLocation.getLongitude(),
                    userLocation.getLatitude(), telegramUser, true, userLocation.getCity());

            resultList.add(infoAboutCity);
        } else {
            weatherSettingsList.forEach(weatherSettings -> {
                String infoAboutCity = saveWeatherInfoAndDoMessageToUser(weatherSettings.getLongitude(),
                        weatherSettings.getLatitude(), telegramUser, false, weatherSettings.getCity());

                resultList.add(infoAboutCity);
            });
        }

        return resultList;
    }

    private String saveWeatherInfoAndDoMessageToUser(float longitude, float latitude,
                                                     TelegramUser user, boolean isUserLocation, String city) {
        String messageToUser;

        YandexWeather weather = yandexWeatherService.getWeatherByCoordinates(Float.toString(longitude),
                Float.toString(latitude));

        if (weather == null) {
            messageToUser = "По вашему месторасположению не найдено информации о погоде!";
        } else {
            if (isUserLocation) {
                saveUserTZ(user, weather);
            }

            YandexWeatherFact fact = weather.getFact();
//            List<YandexWeatherForecast> forecast = weather.getForecasts();

            messageToUser = "Погода в городе: " + "*" + city + "*" + "\r\n\r\n" +
                    "Сейчас " + yandexWeatherService.englishWeatherConditionToRussian(fact.getWeatherCondition()).toLowerCase()
                    + "\r\n" +
                    "Температура воздуха: " + fact.getTemp() + " ℃, по ощущениям: " + fact.getFeelsLike() + " ℃" + "\r\n" +
                    "Влажность: " + fact.getHumidity() + "%";
        }


        return messageToUser;
    }

    @Transactional
    void saveUserTZ(TelegramUser user, YandexWeather weather) {
        YandexWeatherTZInfo tzInfo = weather.getInfo().getTzInfo();
        user.setTzInfo(tzInfo);

        TelegramLocation userLocation = user.getLocation();
        userLocation.setTzInfo(tzInfo);

        userRepository.save(user);
        telegramLocationRepository.save(userLocation);
    }

    @Override
    public void checkNotification() {
    }

    @Override
    public void sendNotificationSettingsKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNotificationSettingsKeyboardMarkup(telegramUser,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }
}
