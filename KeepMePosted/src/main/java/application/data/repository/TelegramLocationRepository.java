package application.data.repository;

import application.data.model.TelegramLocation;
import org.springframework.data.repository.CrudRepository;

public interface TelegramLocationRepository extends CrudRepository<TelegramLocation, Long> {

    TelegramLocation findByLongitudeAndLatitude(float longitude, float latitude);
}
