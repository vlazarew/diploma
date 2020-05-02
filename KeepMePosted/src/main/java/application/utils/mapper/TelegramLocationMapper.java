package application.utils.mapper;

import application.data.model.telegram.TelegramLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;

import javax.annotation.PostConstruct;

@Component
public class TelegramLocationMapper extends AbstractMapper<TelegramLocation, Location> {

    @Autowired
    TelegramLocationMapper() {
        super(TelegramLocation.class, Location.class);
    }

    @PostConstruct
    public void setupMapper() {
//        mapper.createTypeMap(TelegramLocation.class, TelegramLocationDTO.class)
//                .addMapping(m -> m.skip(TelegramLocation::setId)).setPostConverter(toDtoConverter());

    }
}
