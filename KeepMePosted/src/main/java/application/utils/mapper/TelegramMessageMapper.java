package application.utils.mapper;

import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class TelegramMessageMapper extends AbstractMapper<TelegramMessage, Message> {

    @Autowired
    TelegramMessageMapper() {
        super(TelegramMessage.class, Message.class);
    }
}
