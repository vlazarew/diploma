package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherFact;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherFactRepository extends CrudRepository<YandexWeatherFact, Long> {
}
