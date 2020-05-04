package application.utils.handler;

import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class SettingsTelegramHandler extends AbstractTelegramHandler {

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser user = telegramMessage.getFrom();

        if (!hasText) {
            return;
        }

        String userAnswer = telegramMessage.getText();

        switch (userAnswer) {
            case TelegramBot.SETTINGS_BUTTON: {
                sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
                break;
            }
            case TelegramBot.NOTIFICATION_SETTINGS_BUTTON: {
                sendSettingsKeyboard(chatId, "Настройка графика оповещений в разработке");
                break;
            }
            case TelegramBot.WEATHER_SETTINGS_BUTTON: {
                sendWeatherSettingsMessage(chatId, user, "Настройки рассылки погоды", UserStatus.WeatherSettings);
                break;
            }
            case TelegramBot.SETTINGS_BACK_BUTTON: {
                if (user.getStatus() == UserStatus.Settings) {
                    sendMessageToUserByCustomMainKeyboard(chatId, user, "Главная страница", UserStatus.MainPage);
                }
            }
            default: {
                break;
            }

        }
    }

}
