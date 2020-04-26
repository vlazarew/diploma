package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherForecast;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherForecastRepository extends CrudRepository<YandexWeatherForecast, Long> {
}
