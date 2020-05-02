package application.utils.transformer;

import application.data.model.telegram.TelegramLocation;
import application.service.geocoder.YandexGeoCoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;

@Component
public class LocationToTelegramLocationTransformer implements Transformer<Location, TelegramLocation> {

    @Override
    public TelegramLocation transform(Location chat) {
        return TelegramLocation.builder()
//                .creationDate(LocalDateTime.now())
                .latitude(chat.getLatitude())
                .longitude(chat.getLongitude())
                .city(YandexGeoCoder.getCityByCoordinates(chat.getLongitude().toString()
                        + "," + chat.getLatitude().toString()))
                .build();
    }
}
