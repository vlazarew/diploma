package application.service;

import application.data.model.telegram.*;
import application.data.repository.telegram.*;
import application.utils.mapper.*;
import application.utils.transformer.Transformer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.*;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TelegramUpdateService {

    Transformer<Update, TelegramUpdate> updateTelegramUpdateTransformer;
    Transformer<Message, TelegramMessage> messageTelegramMessageTransformer;
    Transformer<User, TelegramUser> userToUserTransformer;
    Transformer<Chat, TelegramChat> chatTelegramChatTransformer;
    Transformer<Contact, TelegramContact> contactTelegramContactTransformer;
    Transformer<Location, TelegramLocation> locationTelegramLocationTransformer;

    TelegramChatRepository telegramChatRepository;
    TelegramMessageRepository telegramMessageRepository;
    TelegramUpdateRepository telegramUpdateRepository;
    TelegramUserRepository userRepository;
    TelegramContactRepository telegramContactRepository;
    TelegramLocationRepository telegramLocationRepository;

    TelegramUserMapper telegramUserMapper;
    TelegramChatMapper telegramChatMapper;
    TelegramContactMapper telegramContactMapper;
    TelegramLocationMapper telegramLocationMapper;
    TelegramMessageMapper telegramMessageMapper;
    TelegramUpdateMapper telegramUpdateMapper;

    // Турбо метод, записывающий все изменения, которые пришли по апдейту
    public TelegramUpdate save(Update update) {

        Message message = update.getMessage();
        boolean hasContact = message.hasContact();
        boolean hasText = message.hasText();
        boolean hasLocation = message.hasLocation();

        // Находим персонажа или создаем его
        TelegramUser telegramUser = saveFindUser(message);

        // Находим или создаем чат
        TelegramChat telegramChat = saveFindChat(message, telegramUser);

        // Сохранение контакта
        TelegramContact telegramContact = null;
        if (hasContact) {
            telegramContact = saveFindContact(message, telegramUser);
        }

        // Сохранение локации
        TelegramLocation telegramLocation = null;
        if (hasLocation) {
            telegramLocation = saveFindLocation(update, telegramUser);
        }

        // Запись истории сообщений
        TelegramMessage telegramMessage = messageTelegramMessageTransformer.transform(update.getMessage());
        telegramMessage.setFrom(telegramUser);
        telegramMessage.setChat(telegramChat);
        telegramMessage.setContact(telegramContact);
        telegramMessage.setLocation(telegramLocation);
        TelegramMessage savedTelegramMessage = telegramMessageRepository.save(telegramMessage);

        // Сохраняем все наши обновления
        TelegramUpdate telegramUpdate = updateTelegramUpdateTransformer.transform(update);
        telegramUpdate.setMessage(savedTelegramMessage);
        return telegramUpdateRepository.save(telegramUpdate);
    }


    private TelegramUser saveFindUser(Message message) {
        return userRepository.findById(message.getFrom().getId())
                .orElseGet(() -> {
                    TelegramUser transformedUser = telegramUserMapper.toEntity(message.getFrom());
                    transformedUser.setStatus(UserStatus.getInitialStatus());
                    return userRepository.save(transformedUser);
                });
    }

    private TelegramChat saveFindChat(Message message, TelegramUser telegramUser) {
        Chat chat = message.getChat();
        return telegramChatRepository.findById(chat.getId())
                .orElseGet(() -> {
                    TelegramChat transformedChat = telegramChatMapper.toEntity(chat);
                    transformedChat.setUser(telegramUser);
                    return telegramChatRepository.save(transformedChat);
                });
    }

    private TelegramContact saveFindContact(Message message, TelegramUser telegramUser) {
        TelegramContact telegramContact = telegramContactRepository.findById(telegramUser.getId())
                .orElseGet(() -> {
                    TelegramContact transformedContact = telegramContactMapper.toEntity(message.getContact());
                    transformedContact.setUser(telegramUser);

                    // Пользователю сохраняем номер телефона
                    setUserPhone(telegramUser, transformedContact);

                    return telegramContactRepository.save(transformedContact);
                });
        return telegramContact;
    }

    private void setUserPhone(TelegramUser telegramUser, TelegramContact transformedContact) {
        telegramUser.setPhone(transformedContact.getPhoneNumber());
        userRepository.save(telegramUser);
    }

    private TelegramLocation saveFindLocation(Update update, TelegramUser telegramUser) {
        Location location = update.getMessage().getLocation();
        float longitude = location.getLongitude();
        float latitude = location.getLatitude();

        TelegramLocation telegramLocation = telegramLocationRepository.findByLongitudeAndLatitude(longitude, latitude);

        if (telegramLocation == null) {
            TelegramLocation transformedLocation = locationTelegramLocationTransformer.transform(location);
            transformedLocation.setUser(telegramUser);
            telegramLocationRepository.save(transformedLocation);

            telegramUser.setLocation(transformedLocation);
            userRepository.save(telegramUser);
        }
        return telegramLocation;
    }


}
