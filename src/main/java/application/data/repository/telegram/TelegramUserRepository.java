package application.data.repository.telegram;

import application.data.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface TelegramUserRepository extends CrudRepository<TelegramUser, Integer> {

    Optional<TelegramUser> findById(Integer id);

}
