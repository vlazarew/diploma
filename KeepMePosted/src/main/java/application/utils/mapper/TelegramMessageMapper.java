package application.utils.mapper;

import antlr.debug.MessageEvent;
import application.data.model.telegram.TelegramMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.PostConstruct;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TelegramMessageMapper extends AbstractMapper<TelegramMessage, Message> {

    ModelMapper mapper;

    @Autowired
    TelegramMessageMapper(ModelMapper mapper) {
        super(TelegramMessage.class, Message.class);
        this.mapper = mapper;
    }

    @PostConstruct
    public void setupMapper() {
        mapper.createTypeMap(Message.class, TelegramMessage.class)
                .addMappings(m -> m.map(Message::getMessageId, TelegramMessage::setId)).setPostConverter(toEntityConverter());
    }
}
