package application.data.model.YandexWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather_hour")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeatherHours {

    @Id
    @GeneratedValue
    Long id;

    // Значение часа, для которого дается прогноз (0-23), локальное время.
    String hour;
    // Время прогноза в Unixtime.
    @JsonProperty("hour_ts")
    Float hourTs;

    // Температура
    Float temp;
    // Ощущаемая температура (°C).
    @JsonProperty("feels_like")
    Float feelsLike;

    // Код иконки погоды. Иконка доступна по адресу https://yastatic.net/weather/i/icons/blueye/color/svg/<значение из поля icon>.svg.
    String icon;
    // Код расшифровки погодного описания
    @JsonProperty("condition")
    String weatherCondition;

    // Скорость ветра (в м/с).
    @JsonProperty("wind_speed")
    Float windSpeed;
    // Скорость порывов ветра (в м/с).
    @JsonProperty("wind_gust")
    Float windGust;
    // Направление ветра.
    @JsonProperty("wind_dir")
    String windDir;

    // Давление (в мм рт. ст.).
    @JsonProperty("pressure_mm")
    Float pressureMm;
    // Давление (в гектопаскалях).
    @JsonProperty("pressure_pa")
    Float pressurePa;

    // Влажность воздуха (в процентах).
    Float humidity;

    // Прогнозируемое количество осадков (в мм).
    @JsonProperty("prec_mm")
    Float precMm;
    // Прогнозируемый период осадков (в минутах).
    @JsonProperty("prec_period")
    Float precPeriod;
    // 	Тип осадков.
    @JsonProperty("prec_type")
    Float precType;
    // Сила осадков.
    @JsonProperty("prec_strength")
    Float precStrength;
    //Облачность.
    Float cloudness;


}
