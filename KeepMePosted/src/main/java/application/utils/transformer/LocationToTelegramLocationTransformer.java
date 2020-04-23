package application.utils.transformer;

import application.data.model.telegram.TelegramLocation;
import application.geodecoder.YandexGeoDecoder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;

@Component
public class LocationToTelegramLocationTransformer implements Transformer<Location, TelegramLocation> {
//    YandexGeoDecoder yandexGeoDecoder;

    @Override
    public TelegramLocation transform(Location chat) {
        return TelegramLocation.builder()
                .creationDate(LocalDateTime.now())
                .latitude(chat.getLatitude())
                .longitude(chat.getLongitude())
                .city(YandexGeoDecoder.getCityByCoordinates(chat.getLongitude().toString()
                        + "," + chat.getLatitude().toString()))
                .build();
    }
}
