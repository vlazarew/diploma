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
@Table(name = "weather_part")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexWeatherParts {

    @Id
    @GeneratedValue
    Long id;

    String name;

    @JsonProperty("_source")
    String source;

    // Минимальная температура для времени суток (°C).
    @JsonProperty("temp_min")
    Float tempMin;
    // Максимальная температура для времени суток (°C).
    @JsonProperty("temp_max")
    Float tempMax;
    // Средняя температура для времени суток (°C).
    @JsonProperty("temp_avg")
    Float tempAvg;
    // Ощущаемая температура (°C).
    @JsonProperty("feels_like")
    Float feelsLike;

    // Код иконки погоды. Иконка доступна по адресу https://yastatic.net/weather/i/icons/blueye/color/svg/<значение из поля icon>.svg.
    String icon;
    // Код расшифровки погодного описания
    @JsonProperty("condition")
    String weatherCondition;
    // Светлое или темное время суток
    String daytime;
    // Признак полярного дня или ночи.
    Boolean polar;

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
    @JsonProperty("prec_strength")
    // Сила осадков.
    Float precStrength;
    //Облачность.
    Float cloudness;
}
