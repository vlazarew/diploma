package application.data.model.YandexWeather;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "weather")
public class YandexWeather {

    @Id
    @GeneratedValue
    Long id;

    // Время сервера в формате Unixtime.
    float now;
    // Время сервера в UTC.
    String nowDateTime;

    @OneToOne
    YandexWeatherInfo info;

    @OneToOne
    YandexWeatherFact fact;

    @OneToMany
    Set<YandexWeatherForecast> forecasts;

}
