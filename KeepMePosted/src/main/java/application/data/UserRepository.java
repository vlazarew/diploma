package application.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    @Query("select u " +
            "from User u " +
            "where u.notified = false and not u.phone is null and not u.email is null")
    List<User> findNewUsers();

    User findByChatId(long id);

}
