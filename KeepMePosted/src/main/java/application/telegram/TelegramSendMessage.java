package application.telegram;


import application.data.model.telegram.TelegramChat;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramUserRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramSendMessage {

    public static void sendTextMessageReplyKeyboardMarkup(Long chatId, String text,
                                                          ReplyKeyboardMarkup replyKeyboardMarkup,
                                                          TelegramBot telegramBot, UserStatus status,
                                                          TelegramUserRepository telegramUserRepository,
                                                          TelegramChatRepository telegramChatRepository) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            telegramBot.execute(sendMessage);

            if (status != null) {
                TelegramUser user = telegramChatRepository.findById(chatId).get().getUser();
                user.setStatus(status);
                telegramUserRepository.save(user);
            }

        } catch (TelegramApiException e) {
            log.error(e);
        }

    }

    public static void sendSettingsKeyboard(Long chatId, String text, TelegramBot telegramBot, UserStatus status,
                                            TelegramUserRepository userRepository, TelegramChatRepository telegramChatRepository) {
        ReplyKeyboardMarkup replyKeyboardMarkup = TelegramKeyboards.getMainSettingsKeyboard();
        TelegramSendMessage.sendTextMessageReplyKeyboardMarkup(chatId, text,
                replyKeyboardMarkup, telegramBot, status, userRepository, telegramChatRepository);
    }

}
