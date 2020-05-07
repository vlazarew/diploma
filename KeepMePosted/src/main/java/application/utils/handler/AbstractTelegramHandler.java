package application.utils.handler;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.service.ServiceSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
@PropertySource("classpath:interface.properties")
public abstract class AbstractTelegramHandler implements TelegramMessageHandler {

    @Autowired
    TelegramBot telegramBot;
    @Autowired
    public TelegramUserRepository userRepository;
    @Autowired
    TelegramChatRepository telegramChatRepository;
    @Autowired
    public ServiceSettingsRepository serviceSettingsRepository;
    @Autowired
    public TelegramKeyboards telegramKeyboards;

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
                TelegramUser user = telegramChatRepository.findById(chatId).get().getUser();
                user.setStatus(status);
                userRepository.save(user);
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
                serviceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getWeatherSettingsKeyboard(user,
                serviceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageAddDeleteCity(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAddDeleteCityKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }
}
