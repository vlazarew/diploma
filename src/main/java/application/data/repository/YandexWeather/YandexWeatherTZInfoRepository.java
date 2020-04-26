package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherTZInfoRepository extends CrudRepository<YandexWeatherTZInfo, Long> {
}
