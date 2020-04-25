package application.data.repository.YandexWeather;

import application.data.model.YandexWeather.YandexWeatherInfo;
import org.springframework.data.repository.CrudRepository;

public interface YandexWeatherInfoRepository extends CrudRepository<YandexWeatherInfo, Long> {
}
