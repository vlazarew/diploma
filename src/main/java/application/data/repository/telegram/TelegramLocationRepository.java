package application.data.repository.telegram;

import application.data.model.telegram.TelegramLocation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface TelegramLocationRepository extends CrudRepository<TelegramLocation, Long> {
    Optional<TelegramLocation> findByLongitudeAndLatitude(float longitude, float latitude);
}
