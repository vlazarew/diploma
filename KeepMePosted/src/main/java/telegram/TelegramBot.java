package telegram;

import data.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.UserService;


@Component
@PropertySource("classpath:telegram.properties")
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(TelegramBot.class);
    private static final String BROADCAST = "broadcast ";
    private static final String LIST_USERS = "users";

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;

    public TelegramBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        User user = userService.findByChatId(chatId);

        if (checkIfAdminCommand(user, text)) {
            return;
        }

        BotContext context;
        BotState state;

        if (user == null) {
            state = BotState.getInitialState();
            user = new User(chatId, state.ordinal());
            userService.addUser(user);

            context = BotContext.of(this, user, text);
            state.enter(context);

            LOGGER.info("Зарегистрирован новый пользователь: " + chatId);
        } else {
            context = BotContext.of(this, user, text);
            state = BotState.byId(user.getStateId());

            LOGGER.info("Обновился статус пользователя в статус: " + state);
        }

        state.handleInput(context);

        do {
            state = state.nextState();
            state.enter(context);
        } while (!state.isInputNeeded());

        user.setStateId(state.ordinal());
        userService.updateUser(user);

    }

    private boolean checkIfAdminCommand(User user, String text) {
        if (user == null || !user.getIsAdmin()) {
            return false;
        }

        if (text.startsWith(BROADCAST)) {
            LOGGER.info("Получена админская команда " + BROADCAST);

            text = text.substring(BROADCAST.length());
            broadcast(text);

            return true;
        } else if (text.equals(LIST_USERS)) {
            LOGGER.info("Получена админская команда " + LIST_USERS);

            listUsers(user);

            return true;
        }

        return false;
    }

}
