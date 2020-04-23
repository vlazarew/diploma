package application.data.repository.telegram;

import application.data.model.telegram.TelegramLocation;
import org.springframework.data.repository.CrudRepository;

public interface TelegramLocationRepository extends CrudRepository<TelegramLocation, Long> {

    TelegramLocation findByLongitudeAndLatitude(float longitude, float latitude);
}
