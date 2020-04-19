package application.telegram;

import application.data.model.TelegramUpdate;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    public static final String HELLO_BUTTON = "Привет";
    public static final String REGISTER_BUTTON = "Зарегистрироваться";
    public static final String HELP_BUTTON = "Помощь";
    //endregion

    @Getter
    @Value("${bot.name}")
    String botName;

    @Getter
    @Value("${bot.token}")
    String botToken;

    final TelegramUpdateService telegramUpdateService;
    final List<TelegramMessageHandler> telegramMessageHandlers;

    @Autowired
    public TelegramBot(TelegramUpdateService telegramUpdateService,
                       @Lazy List<TelegramMessageHandler> telegramMessageHandlers) {
        this.telegramUpdateService = telegramUpdateService;
        this.telegramMessageHandlers = telegramMessageHandlers;
    }

    @Override
    public void onUpdateReceived(Update update) {
        TelegramUpdate telegramUpdate = telegramUpdateService.save(update);
        telegramMessageHandlers.forEach(telegramMessageHandler -> telegramMessageHandler.handle(telegramUpdate));
    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(getCustomReplyKeyboardMarkup());

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private ReplyKeyboardMarkup getCustomReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(HELLO_BUTTON));
        keyboardFirstRow.add(new KeyboardButton(REGISTER_BUTTON));

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton(HELP_BUTTON));

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    //    @Override
//    public String getBotToken() {
//        return botToken;
//    }
//
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
