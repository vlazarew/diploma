package application.utils.transformer;

import application.data.model.TelegramContact;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;

import java.time.LocalDateTime;

@Component
public class ContactToTelegramContactTransformer implements Transformer<Contact, TelegramContact> {


    @Override
    public TelegramContact transform(Contact chat) {
        return TelegramContact.builder()
                .id(chat.getUserID())
                .firstName(chat.getFirstName())
                .lastName(chat.getLastName())
                .phoneNumber(chat.getPhoneNumber())
                .creationDate(LocalDateTime.now())
                .build();
    }
}
