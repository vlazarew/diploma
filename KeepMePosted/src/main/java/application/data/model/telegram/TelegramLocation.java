package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramLocation {

    @Id
    @GeneratedValue
    Long id;

    LocalDateTime creationDate;

    Float longitude;
    Float latitude;
    String city;

    @OneToOne
    TelegramUser user;

    @OneToOne
    YandexWeatherTZInfo tzInfo;
}
