package application.data.model.YandexWeather;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "weather_part")
public class YandexWeatherParts {

    @Id
    @GeneratedValue
    Long id;

    String name;

    String source;

    // Минимальная температура для времени суток (°C).
    Float tempMin;
    // Максимальная температура для времени суток (°C).
    Float tempMax;
    // Средняя температура для времени суток (°C).
    Float tempAvg;
    // Ощущаемая температура (°C).
    Float feelsLike;

    // Код иконки погоды. Иконка доступна по адресу https://yastatic.net/weather/i/icons/blueye/color/svg/<значение из поля icon>.svg.
    String icon;
    // Код расшифровки погодного описания
    String weatherCondition;
    // Светлое или темное время суток
    String daytime;
    // Признак полярного дня или ночи.
    Boolean polar;

    // Скорость ветра (в м/с).
    Float windSpeed;
    // Скорость порывов ветра (в м/с).
    Float windGust;
    // Направление ветра.
    String windDir;

    // Давление (в мм рт. ст.).
    Float pressureMm;
    // Давление (в гектопаскалях).
    Float pressurePa;

    // Влажность воздуха (в процентах).
    Float humidity;


    // Прогнозируемое количество осадков (в мм).
    Float precMm;
    // Прогнозируемый период осадков (в минутах).
    Float precPeriod;
    // 	Тип осадков.
    Float precType;
    // Сила осадков.
    Float precStrength;
    //Облачность.
    Float cloudness;
}
