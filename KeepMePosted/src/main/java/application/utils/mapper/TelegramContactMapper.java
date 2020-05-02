package application.utils.mapper;

import application.data.model.telegram.TelegramContact;
import application.data.model.telegram.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class TelegramContactMapper extends AbstractMapper<TelegramContact, Contact> {

    @Autowired
    TelegramContactMapper() {
        super(TelegramContact.class, Contact.class);
    }
}
