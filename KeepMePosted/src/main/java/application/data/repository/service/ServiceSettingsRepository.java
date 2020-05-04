package application.data.repository.service;

import application.data.model.service.ServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ServiceSettingsRepository extends CrudRepository<ServiceSettings, Long> {

    Optional<ServiceSettings> findByUserAndService(TelegramUser user, WebService webService);

}
