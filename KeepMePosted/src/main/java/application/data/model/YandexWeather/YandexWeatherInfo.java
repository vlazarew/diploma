package application.data.model.YandexWeather;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "weather_info")
public class YandexWeatherInfo {

    @Id
    @GeneratedValue
    Long id;

    // Широта (в градусах).
    Float lat;
    // Долгота (в градусах).
    Float lon;

    // Норма давления для данной координаты (в мм рт. ст.).
    Float defPressureMm;
    // Норма давления для данной координаты (в гектопаскалях).
    Float defPressurePa;

    // Страница населенного пункта на сайте Яндекс.Погода.
    String url;

    @OneToOne
    YandexWeatherTZInfo tzInfo;
}
