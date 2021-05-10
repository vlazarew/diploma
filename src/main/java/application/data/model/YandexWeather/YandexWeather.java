package application.data.model.YandexWeather;

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
@Table(name = "weather")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeather {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    // Время сервера в формате Unixtime.
    float now;
    // Время сервера в UTC.
    @JsonProperty("now_dt")
    String nowDateTime;

    @OneToOne
    YandexWeatherInfo info;

    @OneToOne
    YandexWeatherFact fact;

    @OneToMany
    List<YandexWeatherForecast> forecasts;

}
