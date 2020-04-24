package application.utils.handler;

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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Log4j2
public class SettingsTelegramHandler implements TelegramMessageHandler {
    TelegramBot telegramBot;
    TelegramUserRepository userRepository;
    ServiceSettingsRepository serviceSettingsRepository;
    WeatherSettingsRepository weatherSettingsRepository;
    TelegramChatRepository telegramChatRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean isText, boolean isContact, boolean isLocation) {
        Long chatId = telegramUpdate.getMessage().getChat().getId();
        TelegramUser user = telegramUpdate.getMessage().getFrom();
        UserStatus status = user.getStatus();

        if (isText) {
            String userAnswer = telegramUpdate.getMessage().getText();

            switch (userAnswer) {
                case TelegramBot.SETTINGS_BUTTON: {
                    TelegramSendMessage.sendSettingsKeyboard(chatId, "Настройки бота", telegramBot, UserStatus.Settings,
                            userRepository, telegramChatRepository);
                    break;
                }
                case TelegramBot.NOTIFICATION_SETTINGS_BUTTON: {
                    TelegramSendMessage.sendSettingsKeyboard(chatId, "Настройка графика оповещений в разработке", telegramBot,
                            null, null, null);
                    break;
                }
                case TelegramBot.WEATHER_SETTINGS_BUTTON: {
                    ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getWeatherSettingsKeyboard(user, serviceSettingsRepository);
                    TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, "Настройки рассылки погоды",
                            replyKeyboardMarkup, telegramBot, UserStatus.WeatherSettings, userRepository, telegramChatRepository);
                    break;
                }
                case TelegramBot.SETTINGS_BACK_BUTTON: {
                    if (user.getStatus() == UserStatus.Settings) {
                        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
                        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, "Главная страница",
                                replyKeyboardMarkup, telegramBot, UserStatus.MainPage, userRepository, telegramChatRepository);
                    }
                }
                default: {
                    break;
                }
            }
        }

    }


}
