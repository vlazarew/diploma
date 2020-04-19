package application.service;

import application.data.model.TelegramChat;
import application.data.model.TelegramMessage;
import application.data.model.TelegramUpdate;
import application.data.model.User;
import application.data.repository.TelegramChatRepository;
import application.data.repository.TelegramMessageRepository;
import application.data.repository.TelegramUpdateRepository;
import application.data.repository.UserRepository;
import application.utils.transformer.Transformer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TelegramUpdateService {

    Transformer<Update, TelegramUpdate> updateTelegramUpdateTransformer;
    Transformer<Message, TelegramMessage> messageTelegramMessageTransformer;
    Transformer<org.telegram.telegrambots.meta.api.objects.User, User> userToUserTransformer;
    Transformer<Chat, TelegramChat> chatTelegramChatTransformer;

    TelegramChatRepository telegramChatRepository;
    TelegramMessageRepository telegramMessageRepository;
    TelegramUpdateRepository telegramUpdateRepository;
    UserRepository userRepository;

    // Турбо метод, записывающий все изменения, которые пришли по апдейту
    public TelegramUpdate save(Update update) {

        // Находим персонажа или создаем его
        User telegramUser = userRepository.findById(update.getMessage().getFrom().getId())
                .orElseGet(() -> userRepository.save(userToUserTransformer.transform(update.getMessage().getFrom())));

        // Находим или создаем чат
        TelegramChat telegramChat = telegramChatRepository.findById(update.getMessage().getChat().getId())
                .orElseGet(() -> {
                    TelegramChat transformedChat = chatTelegramChatTransformer.transform(update.getMessage().getChat());
                    transformedChat.setUser(telegramUser);
                    return telegramChatRepository.save(transformedChat);
                });

        // Запись истории сообщений
        TelegramMessage telegramMessage = messageTelegramMessageTransformer.transform(update.getMessage());
        telegramMessage.setFrom(telegramUser);
        telegramMessage.setChat(telegramChat);
        TelegramMessage savedTelegramMessage = telegramMessageRepository.save(telegramMessage);

        // Сохраняем все наши обновления
        TelegramUpdate telegramUpdate = updateTelegramUpdateTransformer.transform(update);
        telegramUpdate.setMessage(savedTelegramMessage);
        return telegramUpdateRepository.save(telegramUpdate);
    }
}
