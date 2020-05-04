package application.service;

import application.data.model.telegram.*;
import application.data.repository.telegram.*;
import application.utils.mapper.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TelegramUpdateService {

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
        boolean hasLocation = message.hasLocation();

        // Находим персонажа или создаем его
        TelegramUser telegramUser = saveFindUser(message);

        // Находим или создаем чат
        TelegramChat telegramChat = saveFindChat(message, telegramUser);

        // Сохранение контакта
        TelegramContact telegramContact = hasContact ? saveFindContact(message, telegramUser) : null;

        // Сохранение локации
        TelegramLocation telegramLocation = hasLocation ? saveFindLocation(update, telegramUser) : null;

        // Запись истории сообщений
        TelegramMessage telegramMessage = saveTelegramMessage(message, telegramUser, telegramChat, telegramContact
                , telegramLocation);

        // Сохраняем все наши обновления
        return saveTelegramUpdate(update, telegramMessage);
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
        return telegramContactRepository.findById(telegramUser.getId())
                .orElseGet(() -> {
                    TelegramContact transformedContact = telegramContactMapper.toEntity(message.getContact());
                    transformedContact.setUser(telegramUser);

                    // Пользователю сохраняем номер телефона
                    setUserPhone(telegramUser, transformedContact);

                    return telegramContactRepository.save(transformedContact);
                });
    }

    private void setUserPhone(TelegramUser telegramUser, TelegramContact transformedContact) {
        telegramUser.setPhone(transformedContact.getPhoneNumber());
        userRepository.save(telegramUser);
    }

    private TelegramLocation saveFindLocation(Update update, TelegramUser telegramUser) {
        Location location = update.getMessage().getLocation();
        float longitude = location.getLongitude();
        float latitude = location.getLatitude();

        return telegramLocationRepository.findByLongitudeAndLatitude(longitude, latitude)
                .orElseGet(() -> {
                    TelegramLocation transformedLocation = telegramLocationMapper.toEntity(location);
                    transformedLocation.setUser(telegramUser);

                    telegramUser.setLocation(transformedLocation);
                    userRepository.save(telegramUser);

                    return telegramLocationRepository.save(transformedLocation);
                });
    }

    private TelegramMessage saveTelegramMessage(Message message, TelegramUser telegramUser, TelegramChat telegramChat,
                                                TelegramContact telegramContact, TelegramLocation telegramLocation) {
        TelegramMessage telegramMessage = telegramMessageMapper.toEntity(message);
        telegramMessage.setFrom(telegramUser);
        telegramMessage.setChat(telegramChat);
        telegramMessage.setContact(telegramContact);
        telegramMessage.setLocation(telegramLocation);
        return telegramMessageRepository.save(telegramMessage);
    }

    private TelegramUpdate saveTelegramUpdate(Update update, TelegramMessage message) {
        TelegramUpdate telegramUpdate = telegramUpdateMapper.toEntity(update);
        telegramUpdate.setMessage(message);
        return telegramUpdateRepository.save(telegramUpdate);
    }
}
