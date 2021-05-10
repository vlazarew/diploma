package application.data.repository.service;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramUser;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

public interface NotificationServiceSettingsRepository extends CrudRepository<NotificationServiceSettings, Long> {

    Optional<NotificationServiceSettings> findByUserAndService(TelegramUser user, WebService webService);

//    @Query("select ServiceSettings.id as serviceId, TelegramChat.id as chatId" +
//            "from ServiceSettings " +
//            "inner join TelegramChat on ServiceSettings.user.id = TelegramChat.user.id " +
//            "where ServiceSettings.active and ServiceSettings.countOfNotificationPerDay > 0")
//    @Query("select ServiceSettings.id as serviceId " +
//            "from ServiceSettings " +
//            "where ServiceSettings.active and ServiceSettings.countOfNotificationPerDay > 0")
//    List<Long> findServiceSettingsToNotification();

    List<NotificationServiceSettings> findAllByActiveIsTrueAndCountOfNotificationPerDayGreaterThan(@Min(0) Integer countOfNotificationPerDay);

    List<NotificationServiceSettings> findByUser(TelegramUser user);
}
