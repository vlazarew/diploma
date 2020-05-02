package application.telegram;

import application.data.model.telegram.TelegramUpdate;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.service.TelegramUpdateService;
import application.utils.handler.TelegramMessageHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:telegram.properties")
public class TelegramBot extends TelegramLongPollingBot {

    //region Кнопки в приложении
    public static final String START_COMMAND = "/start";

    // Базовые кнопки
    public static final String HELLO_BUTTON = "Привет";
    public static final String HELP_BUTTON = "Помощь";

    // Кнопки регистрации
    public static final String REGISTER_BUTTON = "Зарегистрироваться";
    public static final String CANCEL_REGISTRATION_BUTTON = "Отмена регистрации";
    public static final String NEXT_BUTTON = "Продолжить";
    public static final String SHARE_PHONE_NUMBER = "Поделиться номером";
    public static final String CONFIRM_EMAIL = "Подтвердить e-mail";

    // Кнопки настроек
    public static final String SETTINGS_BUTTON = "Настройки";
    public static final String NOTIFICATION_SETTINGS_BUTTON = "Настройки оповещений";
    public static final String SETTINGS_BACK_BUTTON = "Назад";

    // Интеграция с погодой
    public static final String WEATHER_SETTINGS_BUTTON = "Настройки рассылки погоды";
    public static final String ACTIVATE_WEATHER_BUTTON = "Использовать рассылку погоды";
    public static final String DEACTIVATE_WEATHER_BUTTON = "Отключить рассылку погоды";
    public static final String SHARE_LOCATION_BUTTON = "Поделиться локацией";
    public static final String ADD_CITY_WEATHER_BUTTON = "Добавить город";
    public static final String REMOVE_CITY_WEATHER_BUTTON = "Удалить город";
    public static final String LIST_FOLLOWING_CITIES_BUTTON = "Список отслеживаемых городов";
    public static final String CANCEL_BUTTON = "Отмена";
    public static final String WEATHER_IN_CURRENT_LOCATION_BUTTON = "Показать погоду в текущей локации";
    //endregion

    @Getter
    @Value("${bot.name}")
    String botName;

    @Getter
    @Value("${bot.token}")
    String botToken;

    final TelegramUserRepository userRepository;
    final TelegramChatRepository telegramChatRepository;
    final TelegramUpdateService telegramUpdateService;
    final List<TelegramMessageHandler> telegramMessageHandlers;

    @Autowired
    public TelegramBot(TelegramUpdateService telegramUpdateService,
                       @Lazy List<TelegramMessageHandler> telegramMessageHandlers, TelegramUserRepository userRepository,
                       TelegramChatRepository telegramChatRepository) {
        this.telegramUpdateService = telegramUpdateService;
        this.userRepository = userRepository;
        this.telegramMessageHandlers = telegramMessageHandlers;
        this.telegramChatRepository = telegramChatRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        boolean isContact = message.hasContact(); //has prefix
        boolean isText = message.hasText();
        boolean isLocation = message.hasLocation();

        TelegramUpdate telegramUpdate = telegramUpdateService.save(update);
        telegramMessageHandlers.forEach(telegramMessageHandler -> telegramMessageHandler.handle(telegramUpdate, isText,
                isContact, isLocation));
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

}
