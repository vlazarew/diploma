package application.data.model.YandexWeather;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "weather_forecast")
public class YandexWeatherForecast {

    @Id
    @GeneratedValue
    Long id;

    // Дата прогноза в формате ГГГГ-ММ-ДД.
    String date;
    // Дата прогноза в формате Unixtime.
    Float dateTs;
    // Порядковый номер недели.
    Float week;

    // Время восхода Солнца, локальное время (может отсутствовать для полярных регионов).
    String sunrise;
    // Время заката Солнца, локальное время (может отсутствовать для полярных регионов).
    String sunset;

    // Код фазы Луны.
    Integer moonCode;
    // Текстовый код для фазы Луны.
    String moonText;

    @OneToMany(cascade = CascadeType.ALL)
    Set<YandexWeatherParts> parts;

    @OneToMany(cascade = CascadeType.ALL)
    Set<YandexWeatherHours> hours;
}
