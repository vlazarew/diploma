package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import application.service.geocoder.YandexGeoCoderService;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "location_user_id_index"),
        @Index(columnList = "longitude", name = "location_longitude_index"),
        @Index(columnList = "latitude", name = "location_latitude_index")})
public class TelegramLocation extends AbstractTelegramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "longitude")
    Float longitude;
    @Column(name = "latitude")
    Float latitude;
    String city;

    @OneToOne
    @JoinColumn(name = "user_id")
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
