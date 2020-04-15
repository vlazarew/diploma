package data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query("select u " +
            "from User u " +
            "where not u.notified and u.phone is not null and u.email is not null ")
    List<User> findNewUsers();

    User findByChatId(long id);

}
