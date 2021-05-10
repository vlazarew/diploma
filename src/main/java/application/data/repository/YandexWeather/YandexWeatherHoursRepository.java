package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherHours;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherHoursRepository extends CrudRepository<YandexWeatherHours, Long> {
}
