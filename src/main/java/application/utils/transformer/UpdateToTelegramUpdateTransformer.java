package application.utils.transformer;

import application.data.model.telegram.TelegramUpdate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Component
public class UpdateToTelegramUpdateTransformer implements Transformer<Update, TelegramUpdate> {
    @Override
    public TelegramUpdate transform(Update chat) {
        return TelegramUpdate.builder()
                .id(chat.getUpdateId())
                .creationDate(LocalDateTime.now())
                .build();
    }
}
