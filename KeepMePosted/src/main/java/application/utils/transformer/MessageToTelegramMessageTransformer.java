package application.utils.transformer;

import application.data.model.telegram.TelegramMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;

@Component
public class MessageToTelegramMessageTransformer implements Transformer<Message, TelegramMessage> {
    @Override
    public TelegramMessage transform(Message chat) {
        return TelegramMessage.builder()
                .id(chat.getMessageId())
//                .creationDate(LocalDateTime.now())
                .text(chat.getText())
                .build();
    }
}
