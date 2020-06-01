package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.WeatherCity;
import org.springframework.data.repository.CrudRepository;

public interface WeatherCityRepository extends CrudRepository<WeatherCity, Long> {

    WeatherCity findByName(String name);

}
