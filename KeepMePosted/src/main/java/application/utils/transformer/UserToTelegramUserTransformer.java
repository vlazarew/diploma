package application.utils.transformer;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Component
public class UserToTelegramUserTransformer implements Transformer<User, application.data.model.User> {
    @Override
    public application.data.model.User transform(User chat) {
        return application.data.model.User.builder()
                .id(chat.getId())
                .creationDate(LocalDateTime.now())
                .userName(chat.getUserName())
                .bot(chat.getBot())
                .lastName(chat.getLastName())
                .firstName(chat.getFirstName())
                .languageCode(chat.getLanguageCode())
                .build();
    }
}
