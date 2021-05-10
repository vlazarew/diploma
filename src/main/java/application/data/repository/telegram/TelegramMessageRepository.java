package application.data.repository.telegram;

import application.data.model.telegram.TelegramMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TelegramMessageRepository extends CrudRepository<TelegramMessage, Integer> {
}
