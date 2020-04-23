package application.service;

import application.data.model.telegram.*;
import application.data.repository.telegram.*;
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

    // Турбо метод, записывающий все изменения, которые пришли по апдейту
    public TelegramUpdate save(Update update) {

        boolean isContact = update.getMessage().hasContact();
        boolean isText = update.getMessage().hasText();
        boolean isLocation = update.getMessage().hasLocation();

        // Находим персонажа или создаем его
        TelegramUser telegramUser = userRepository.findById(update.getMessage().getFrom().getId())
                .orElseGet(() -> {
                    TelegramUser transformedUser = userToUserTransformer.transform(update.getMessage().getFrom());
                    transformedUser.setStatus(UserStatus.getInitialStatus());
                    return userRepository.save(transformedUser);
                });

        // Находим или создаем чат
        TelegramChat telegramChat = telegramChatRepository.findById(update.getMessage().getChat().getId())
                .orElseGet(() -> {
                    TelegramChat transformedChat = chatTelegramChatTransformer.transform(update.getMessage().getChat());
                    transformedChat.setUser(telegramUser);
                    return telegramChatRepository.save(transformedChat);
                });

        TelegramContact telegramContact = null;
        if (isContact) {
            // Сохранение контакта
            telegramContact = telegramContactRepository.findById(update.getMessage().getFrom().getId())
                    .orElseGet(() -> {
                        TelegramContact transformedContact = contactTelegramContactTransformer.transform(update.getMessage().getContact());
                        transformedContact.setUser(telegramUser);

                        // Пользователю сохраняем номер телефона
                        telegramUser.setPhone(transformedContact.getPhoneNumber());
                        userRepository.save(telegramUser);

                        return telegramContactRepository.save(transformedContact);
                    });
        }

        TelegramLocation telegramLocation;
        if (isLocation) {
            float longitude = update.getMessage().getLocation().getLongitude();
            float latitude = update.getMessage().getLocation().getLatitude();
            // Сохранение локации
            telegramLocation = telegramLocationRepository.findByLongitudeAndLatitude(longitude, latitude);

            if (telegramLocation == null) {
                TelegramLocation transformedLocation = locationTelegramLocationTransformer.transform(update.getMessage().getLocation());
                transformedLocation.setUser(telegramUser);
                telegramLocationRepository.save(transformedLocation);

                telegramUser.setLocation(transformedLocation);
                userRepository.save(telegramUser);
            }
        }

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
