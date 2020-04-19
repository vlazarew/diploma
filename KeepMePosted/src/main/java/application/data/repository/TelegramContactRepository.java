package application.data.repository;

import application.data.model.TelegramContact;
import org.springframework.data.repository.CrudRepository;

public interface TelegramContactRepository extends CrudRepository<TelegramContact, Integer> {
}
