package application.telegram;

import application.data.model.TelegramChat;
import application.data.model.TelegramUpdate;
import application.data.model.TelegramUser;
import application.data.model.UserStatus;
import application.data.repository.TelegramChatRepository;
import application.data.repository.UserRepository;
import application.service.TelegramUpdateService;
import application.utils.handler.TelegramMessageHandler;
import com.google.inject.internal.cglib.proxy.$UndeclaredThrowableException;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:telegram.properties")
public class TelegramBot extends TelegramLongPollingBot {

//    private static final Logger LOGGER = LogManager.getLogger(TelegramBot.class);
//    private static final String BROADCAST = "broadcast ";
//    private static final String LIST_USERS = "users";

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

    // Интеграция с погодой
    public static final String WEATHER_SETTINGS_BUTTON = "Настройки рассылки погоды";
    public static final String ACTIVATE_WEATHER_BUTTON = "Использовать рассылку погоды";
    public static final String DEACTIVATE_WEATHER_BUTTON = "Отключить рассылку погоды";
    public static final String SHARE_LOCATION_BUTTON = "Поделиться локацией";
    public static final String ADD_CITY_WEATHER_BUTTON = "Добавить город";
    public static final String REMOVE_CITY_WEATHER_BUTTON = "Удалить город";
    //endregion

    @Getter
    @Value("${bot.name}")
    String botName;

    @Getter
    @Value("${bot.token}")
    String botToken;

    final UserRepository userRepository;
    final TelegramChatRepository telegramChatRepository;
    final TelegramUpdateService telegramUpdateService;
    final List<TelegramMessageHandler> telegramMessageHandlers;

    @Autowired
    public TelegramBot(TelegramUpdateService telegramUpdateService,
                       @Lazy List<TelegramMessageHandler> telegramMessageHandlers, UserRepository userRepository,
                       TelegramChatRepository telegramChatRepository) {
        this.telegramUpdateService = telegramUpdateService;
        this.userRepository = userRepository;
        this.telegramMessageHandlers = telegramMessageHandlers;
        this.telegramChatRepository = telegramChatRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {

        boolean isContact = update.getMessage().hasContact();
        boolean isText = update.getMessage().hasText();
        boolean isLocation = update.getMessage().hasLocation();

        TelegramUpdate telegramUpdate = telegramUpdateService.save(update);
        telegramMessageHandlers.forEach(telegramMessageHandler -> telegramMessageHandler.handle(telegramUpdate, isText,
                isContact, isLocation));
    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        TelegramUser user = telegramChatRepository.findById(chatId).get().getUser();

        sendMessage.setReplyMarkup(getCustomReplyKeyboardMarkup(user));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup getCustomReplyKeyboardMarkup(TelegramUser user) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        if (user.getRegistered() != null && user.getRegistered()) {
            keyboardFirstRow.add(new KeyboardButton(SETTINGS_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(HELP_BUTTON));

            keyboard.add(keyboardFirstRow);
        } else {
            keyboardFirstRow.add(new KeyboardButton(HELLO_BUTTON));
            keyboardFirstRow.add(new KeyboardButton(REGISTER_BUTTON));

            KeyboardRow keyboardSecondRow = new KeyboardRow();
            keyboardSecondRow.add(new KeyboardButton(HELP_BUTTON));

            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        if (!update.hasMessage() || !update.getMessage().hasText()) {
//            return;
//        }
//
//        String text = update.getMessage().getText();
//        long chatId = update.getMessage().getChatId();
//
//        User user = telegramUpdateService.findByChatId(chatId);
//
//        if (checkIfAdminCommand(user, text)) {
//            return;
//        }
//
//        BotContext context;
//        BotState state;
//
//        if (user == null) {
//            state = BotState.getInitialState();
//            user = new User(chatId, state.ordinal());
//            telegramUpdateService.addUser(user);
//
//            context = BotContext.of(this, user, text);
//            state.enter(context);
//
//            LOGGER.info("Зарегистрирован новый пользователь: " + chatId);
//        } else {
//            context = BotContext.of(this, user, text);
//            state = BotState.byId(user.getStateId());
//
//            LOGGER.info("Обновился статус пользователя в статус: " + state);
//        }
//
//        state.handleInput(context);
//
//        do {
//            state = state.nextState();
//            state.enter(context);
//        } while (!state.isInputNeeded());
//
//        user.setStateId(state.ordinal());
//        telegramUpdateService.updateUser(user);
//
//    }
//
//    private boolean checkIfAdminCommand(User user, String text) {
//        if (user == null || !user.getIsAdmin()) {
//            return false;
//        }
//
//        if (text.startsWith(BROADCAST)) {
//            LOGGER.info("Получена админская команда " + BROADCAST);
//
//            text = text.substring(BROADCAST.length());
//            broadcast(text);
//
//            return true;
//        } else if (text.equals(LIST_USERS)) {
//            LOGGER.info("Получена админская команда " + LIST_USERS);
//
//            listUsers(user);
//
//            return true;
//        }
//
//        return false;
//    }
//
//    private void sendMessage(long chatId, String text) {
//        SendMessage message = new SendMessage().setChatId(chatId).setText(text);
//
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void broadcast(String text) {
//        List<User> users = telegramUpdateService.findAllUsers();
//        users.forEach(user -> sendMessage(user.getChatId(), text));
//    }
//
//    private void listUsers(User admin) {
//        StringBuilder stringBuilder = new StringBuilder("Список всех пользователей:\r\n");
//        List<User> users = telegramUpdateService.findAllUsers();
//
//        users.forEach(user -> stringBuilder.append(user.getId())
//                .append(' ')
//                .append(user.getPhone())
//                .append(' ')
//                .append(user.getEmail())
//                .append("\r\n"));
//
//        sendMessage(admin.getChatId(), stringBuilder.toString());
//    }

}
