package application.controller;

import application.data.repository.TelegramChatRepository;
import application.telegram.TelegramBot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SendMessageController {

    TelegramBot telegramBot;
    TelegramChatRepository telegramChatRepository;

    @PostMapping("/user/{userId}/send-message")
    @ResponseStatus(HttpStatus.OK)
    public void sendToUser(@PathVariable Integer userId, @RequestBody String message) {
        telegramChatRepository.findByUserId(userId)
                .ifPresent(telegramChat -> telegramBot.sendTextMessage(telegramChat.getId(), message));
    }

//    @PostMapping("/person/{personId}/send-message")
//    @ResponseStatus(HttpStatus.OK)
//    public void sendToPerson(@PathVariable Integer personId, @RequestBody String message) {
//        telegramChatRepository.findByUserPersonId(personId)
//                .ifPresent(telegramChat -> telegramBot.sendTextMessage(telegramChat.getId(), message));
//    }

    @PostMapping("/user/send-messages")
    @ResponseStatus(HttpStatus.OK)
    public void sendToAllUsers(@RequestBody String message) {
        telegramChatRepository.findAll()
                .forEach(telegramChat -> telegramBot.sendTextMessage(telegramChat.getId(), message));
    }
}
