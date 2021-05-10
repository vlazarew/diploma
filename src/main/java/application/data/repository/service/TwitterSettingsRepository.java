package application.data.repository.service;

import application.data.model.service.TwitterSettings;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TwitterSettingsRepository extends CrudRepository<TwitterSettings, Long> {

    TwitterSettings findByUserId(Integer id);

}
