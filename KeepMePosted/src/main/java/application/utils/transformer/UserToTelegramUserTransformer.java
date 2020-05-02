package application.utils.transformer;

import application.data.model.telegram.TelegramUser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Component
public class UserToTelegramUserTransformer implements Transformer<User, TelegramUser> {
    @Override
    public TelegramUser transform(User chat) {
        return TelegramUser.builder()
                .id(chat.getId())
//                .creationDate(LocalDateTime.now())
                .userName(chat.getUserName())
                .bot(chat.getBot())
                .lastName(chat.getLastName())
                .firstName(chat.getFirstName())
                .languageCode(chat.getLanguageCode())
                .build();
    }
}
