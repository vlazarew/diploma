package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.WeatherCity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;

public interface WeatherCityRepository extends CrudRepository<WeatherCity, Long> {

    WeatherCity findByName(String name);

    WeatherCity findTop1ByCreationDateBeforeOrderByCreationDateDesc(LocalDateTime creationDate);

}
