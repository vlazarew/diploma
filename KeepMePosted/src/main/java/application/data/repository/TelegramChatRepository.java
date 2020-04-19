package application.data.repository;

import application.data.model.TelegramChat;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface TelegramChatRepository extends CrudRepository<TelegramChat, Long> {

    Optional<TelegramChat> findByUserId(Integer id);

    //    @Query("select p from Person p where p.id = :personId ")
    //    Optional<TelegramChat> findByUserPersonId(@Param("personId") Integer id);
    Optional<TelegramChat> findByUserPersonId(Integer id);

}
