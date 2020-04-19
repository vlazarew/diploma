package application.data.repository;

import application.data.model.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource
public interface PersonRepository extends CrudRepository<Person, Integer> {

    Optional<Person> findByAuthCode(String authCode);

}
