package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramLocation extends AbstractTelegramEntity {

    @Id
    @GeneratedValue
    Long id;

    Float longitude;
    Float latitude;
    String city;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramUser user;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    YandexWeatherTZInfo tzInfo;
}
