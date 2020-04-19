package application.service;

import application.data.model.*;
import application.data.repository.*;
import application.utils.transformer.Transformer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Contact;
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
    Transformer<Contact, TelegramContact> contactTelegramContactTransformer;

    TelegramChatRepository telegramChatRepository;
    TelegramMessageRepository telegramMessageRepository;
    TelegramUpdateRepository telegramUpdateRepository;
    UserRepository userRepository;
    TelegramContactRepository telegramContactRepository;

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

        TelegramContact telegramContact = telegramContactRepository.findById(update.getMessage().getFrom().getId())
                .orElseGet(() -> {
                   TelegramContact transformedContact = contactTelegramContactTransformer.transform(update.getMessage().getContact());
                   transformedContact.setUser(telegramUser);
                   return telegramContactRepository.save(transformedContact);
                });

        // Запись истории сообщений
        TelegramMessage telegramMessage = messageTelegramMessageTransformer.transform(update.getMessage());
        telegramMessage.setFrom(telegramUser);
        telegramMessage.setChat(telegramChat);
        telegramMessage.setContact(telegramContact);
        TelegramMessage savedTelegramMessage = telegramMessageRepository.save(telegramMessage);

        // Сохраняем все наши обновления
        TelegramUpdate telegramUpdate = updateTelegramUpdateTransformer.transform(update);
        telegramUpdate.setMessage(savedTelegramMessage);
        return telegramUpdateRepository.save(telegramUpdate);
    }
}
