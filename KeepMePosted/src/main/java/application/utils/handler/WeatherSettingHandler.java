package application.utils.handler;

import application.data.model.service.ServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
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

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isLocation) {

            if (user.getLocation().getCity() == null) {
                sendCityMessage(user, chatId, "По вашей геопозиции не найден город. Добавьте его вручную", null);
                return;
            }

            String city = user.getLocation().getCity();

            String messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, city) ?
                    "Город " + city + " добавлен в список отслеживаемых" : "Город " + city + " уже отслеживается вами";

            sendCityMessage(user, chatId, messageToUser, null);
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

                    sendCityMessage(user, chatId, "Оповещения включены", null);
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

                    sendCityMessage(user, chatId, "Оповещения выключены", null);
                    break;
                }
                case TelegramBot.ADD_CITY_WEATHER_BUTTON: {

                    break;
                }
                case TelegramBot.LIST_FOLLOWING_CITIES_BUTTON: {
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void sendCityMessage(TelegramUser user, Long chatId, String text, UserStatus status) {
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
            weatherSettings = new WeatherSettings();
            weatherSettings.setCity(city);
            weatherSettings.setUser(user);
            weatherSettings.setServiceSettings(serviceSettings);

            weatherSettingsRepository.save(weatherSettings);
        }

        return needToCreateNewRule;
    }

//    private void sendAddDeleteCityMessage(Long chatId, String text) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.enableMarkdown(true);
//        sendMessage.setChatId(chatId);
//        sendMessage.setText(text);
//
//        sendMessage.setReplyMarkup(getCityKeyboardMarkup());
//
//        try {
//            telegramBot.execute(sendMessage);
//        } catch (TelegramApiException e) {
//            log.error(e);
//        }
//    }


}
