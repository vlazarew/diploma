package application.utils.handler;

import application.data.model.service.ServiceSettings;
import application.data.model.service.WeatherSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isLocation) {

            if (user.getLocation().getCity() == null) {
                settingsTelegramHandler.sendWeatherSettingKeyboard(chatId, user, "По вашей геопозиции не найден город. Добавьте его вручную");
                return;
            }

            String city = user.getLocation().getCity();

            String messageToUser = addWeatherSettingsToUser(user, WebService.YandexWeather, city) ?
                    "Город " + city + " добавлен в список отслеживаемых" : "Город " + city + " уже отслеживается вами";

            settingsTelegramHandler.sendWeatherSettingKeyboard(chatId, user, messageToUser);
        } else if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.SETTINGS_BACK_BUTTON: {
                    settingsTelegramHandler.sendSettingsKeyboard(chatId, user, "Настройки бота");
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

                    settingsTelegramHandler.sendWeatherSettingKeyboard(chatId, user, "Оповещения включены");
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

                    settingsTelegramHandler.sendWeatherSettingKeyboard(chatId, user, "Оповещения выключены");
                    break;
                }
                default: {
                    break;
                }
            }
        }
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
}
