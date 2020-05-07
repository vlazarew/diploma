package application.controller;

import application.data.model.telegram.TelegramUser;
import application.data.repository.telegram.TelegramChatRepository;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import application.utils.handler.AbstractTelegramHandler;
import application.utils.mapper.AbstractMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SendMessageController extends AbstractTelegramHandler {

    @Autowired
    TelegramChatRepository telegramChatRepository;
    @Autowired
    TelegramKeyboards telegramKeyboards;

    @PostMapping("/user/{userId}/send-message")
    @ResponseStatus(HttpStatus.OK)
    public void sendToUser(@PathVariable Integer userId, @RequestBody String message) {
        telegramChatRepository.findByUserId(userId)
                .ifPresent(telegramChat -> {
                    TelegramUser user = telegramChat.getUser();
                    ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
                    sendTextMessageReplyKeyboardMarkup(telegramChat.getId(), message,
                            replyKeyboardMarkup, null);
                });
    }

    @PostMapping("/user/send-messages")
    @ResponseStatus(HttpStatus.OK)
    public void sendToAllUsers(@RequestBody String message) {
        telegramChatRepository.findAll()
                .forEach(telegramChat -> {
                    TelegramUser user = telegramChat.getUser();
                    ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(user);
                    sendTextMessageReplyKeyboardMarkup(telegramChat.getId(), message,
                            replyKeyboardMarkup, null);
                });
    }
}
