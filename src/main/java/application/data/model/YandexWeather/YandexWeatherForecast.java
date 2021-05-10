package application.data.model.YandexWeather;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather_forecast")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeatherForecast {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    // Дата прогноза в формате ГГГГ-ММ-ДД.
    String date;
    // Дата прогноза в формате Unixtime.
    @JsonProperty("date_ts")
    Float dateTs;
    // Порядковый номер недели.
    Float week;

    // Время восхода Солнца, локальное время (может отсутствовать для полярных регионов).
    String sunrise;
    // Время заката Солнца, локальное время (может отсутствовать для полярных регионов).
    String sunset;

    // Код фазы Луны.
    @JsonProperty("moon_code")
    Integer moonCode;
    // Текстовый код для фазы Луны.
    @JsonProperty("moon_text")
    String moonText;

    @OneToMany(cascade = CascadeType.MERGE)
    @JsonIgnore
    List<YandexWeatherParts> parts;

    @OneToMany(cascade = CascadeType.MERGE)
    List<YandexWeatherHours> hours;
}
