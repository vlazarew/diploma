package application.data.repository.telegram;

import application.data.model.telegram.TelegramContact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TelegramContactRepository extends CrudRepository<TelegramContact, Integer> {
}
