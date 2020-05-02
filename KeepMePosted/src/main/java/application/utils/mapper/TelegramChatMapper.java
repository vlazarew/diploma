package application.utils.mapper;

import application.data.model.telegram.TelegramChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Component
public class TelegramChatMapper extends AbstractMapper<TelegramChat, Chat> {

    @Autowired
    public TelegramChatMapper() {
        super(TelegramChat.class, Chat.class);
    }

}
