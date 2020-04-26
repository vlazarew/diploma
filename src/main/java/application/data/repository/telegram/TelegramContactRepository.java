package application.data.repository.telegram;

import application.data.model.telegram.TelegramContact;
import org.springframework.data.repository.CrudRepository;

public interface TelegramContactRepository extends CrudRepository<TelegramContact, Integer> {
}
