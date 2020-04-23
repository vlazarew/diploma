package application.data.repository.service;

import application.data.model.service.ServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;

public interface ServiceSettingsRepository extends CrudRepository<ServiceSettings, Long> {

    ServiceSettings findByUserAndService(TelegramUser user, WebService webService);

}
