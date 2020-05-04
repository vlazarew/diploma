package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import application.service.geocoder.YandexGeoCoderService;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class TelegramLocation extends AbstractTelegramEntity {

    @Id
    @GeneratedValue
    Long id;

    Float longitude;
    Float latitude;
    String city;

    @OneToOne
    TelegramUser user;

    @OneToOne
    YandexWeatherTZInfo tzInfo;

    @Override
    public void toCreate() {
        YandexGeoCoderService yandexGeoCoderService = new YandexGeoCoderService();
        super.toCreate();
        this.city = yandexGeoCoderService.getCityByCoordinates(this.longitude.toString()
                + "," + this.latitude.toString());
    }
}
