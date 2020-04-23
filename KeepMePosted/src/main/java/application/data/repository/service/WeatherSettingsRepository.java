package application.data.repository.service;

import application.data.model.service.WeatherSettings;
import org.springframework.data.repository.CrudRepository;

public interface WeatherSettingsRepository extends CrudRepository<WeatherSettings, Long> {

    WeatherSettings findByUserIdAndCity(Integer userId, String city);

}
