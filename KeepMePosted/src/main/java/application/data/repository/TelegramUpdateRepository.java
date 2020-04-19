package application.data.repository;

import application.data.model.TelegramUpdate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TelegramUpdateRepository extends CrudRepository<TelegramUpdate, Integer> {
}
