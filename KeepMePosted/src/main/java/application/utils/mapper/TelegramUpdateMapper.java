package application.utils.mapper;

import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class TelegramUpdateMapper extends AbstractMapper<TelegramUpdate, Update> {

    @Autowired
    TelegramUpdateMapper() {
        super(TelegramUpdate.class, Update.class);
    }
}
