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
@Table(name = "weather_fact")
public class YandexWeatherFact {

    @Id
    @GeneratedValue
    Long id;

    // Температура (°C).
    Float temp;
    // Ощущаемая температура (°C).
    Float feelsLike;
    // 	Температура воды (°C).
    Float tempWater;

    // Код иконки погоды. Иконка доступна по адресу https://yastatic.net/weather/i/icons/blueye/color/svg/<значение из поля icon>.svg.
    String icon;
    // 	Код расшифровки погодного описания.
    String weatherCondition;

    // Скорость ветра (в м/с).
    Float windSpeed;
    // Скорость порывов ветра (в м/с).
    Float windGust;
    // 	Направление ветра
    String windDir;

    // Давление (в мм рт. ст.).
    Float pressureMm;
    // Давление (в гектопаскалях).
    Float pressurePa;

    // Влажность воздуха (в процентах).
    Float humidity;
    // 	Светлое или темное время суток.
    String daytime;
    // Признак полярного дня или ночи.
    Boolean polar;
    // 	Время года в данном населенном пункте.
    String season;

    // Время замера погодных данных в формате Unixtime.
    Float obsTime;

    // Тип осадков.
    Float precType;
    // Сила осадков.
    Float precStrength;
    // Облачность.
    Float cloudness;

}
