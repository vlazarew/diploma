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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
public abstract class AbstractTelegramHandler implements TelegramMessageHandler {

    @Autowired
    TelegramBot telegramBot;
    @Autowired
    TelegramUserRepository userRepository;
    @Autowired
    TelegramChatRepository telegramChatRepository;
    @Autowired
    ServiceSettingsRepository serviceSettingsRepository;

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
    }

    @Override
    public void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendSettingsKeyboard(Long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getMainSettingsKeyboard();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendSettingsKeyboard(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getMainSettingsKeyboard();
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
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getWeatherSettingsKeyboard(user,
                serviceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, null);
    }

    @Override
    public void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getWeatherSettingsKeyboard(user,
                serviceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageAddDeleteCity(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getAddDeleteCityKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }
}
