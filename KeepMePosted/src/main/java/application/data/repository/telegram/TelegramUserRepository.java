package application.data.repository.telegram;

import application.data.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface TelegramUserRepository extends CrudRepository<TelegramUser, Integer> {

    Optional<TelegramUser> findById(Integer id);

//    @Query("select u " +
//            "from User u " +
//            "where u.notified = false and not u.phone is null and not u.email is null")
//    List<User> findNewUsers();
//
//    User findByChatId(long id);

}
