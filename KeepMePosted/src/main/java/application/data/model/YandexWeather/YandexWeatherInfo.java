package application.data.model.YandexWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather_info")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeatherInfo {

    @Id
    @GeneratedValue
    Long id;

    // Широта (в градусах).
    Float lat;
    // Долгота (в градусах).
    Float lon;

    // Норма давления для данной координаты (в мм рт. ст.).
    @JsonProperty("def_pressure_mm")
    Float defPressureMm;
    // Норма давления для данной координаты (в гектопаскалях).
    @JsonProperty("def_pressure_pa")
    Float defPressurePa;

    // Страница населенного пункта на сайте Яндекс.Погода.
    String url;

    @OneToOne
    @JsonProperty("tzinfo")
    YandexWeatherTZInfo tzInfo;
}
