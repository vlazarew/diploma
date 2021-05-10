package application.data.model.YandexWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather_TZ_Info")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeatherTZInfo {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    // Часовой пояс в секундах от UTC.
    Float offset;
    // Название часового пояса.
    String name;
    // Сокращенное название часового пояса.
    String abbr;
    // Признак летнего времени.
    Boolean dst;
}
