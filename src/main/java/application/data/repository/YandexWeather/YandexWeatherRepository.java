package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeather;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherRepository extends CrudRepository<YandexWeather, Long> {
}
