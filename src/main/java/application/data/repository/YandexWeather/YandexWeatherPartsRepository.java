package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherParts;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherPartsRepository extends CrudRepository<YandexWeatherParts, Long> {
}
