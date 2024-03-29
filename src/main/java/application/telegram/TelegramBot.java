package application.telegram;

import application.data.model.telegram.TelegramUpdate;
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
        Message message = update.getMessage();
        boolean hasContact = message.hasContact();
        boolean hasText = message.hasText();
        boolean hasLocation = message.hasLocation();

        TelegramUpdate telegramUpdate = telegramUpdateService.save(update);
        telegramMessageHandlers.forEach(telegramMessageHandler -> telegramMessageHandler.handle(telegramUpdate, hasText,
                hasContact, hasLocation));
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

}
