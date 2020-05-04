package application.utils.mapper;

import application.data.model.telegram.TelegramContact;
import application.data.model.telegram.TelegramMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.PostConstruct;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramContactMapper extends AbstractMapper<TelegramContact, Contact> {

    ModelMapper mapper;

    @Autowired
    TelegramContactMapper(ModelMapper mapper) {
        super(TelegramContact.class, Contact.class);
        this.mapper = mapper;
    }

    @PostConstruct
    public void setupMapper() {
        mapper.createTypeMap(Contact.class, TelegramContact.class)
                .addMappings(m -> m.map(Contact::getUserID, TelegramContact::setId)).setPostConverter(toEntityConverter());
    }
}
