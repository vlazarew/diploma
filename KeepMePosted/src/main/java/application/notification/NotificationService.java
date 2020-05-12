package application.notification;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.telegram.TelegramUser;
import application.utils.handler.AbstractTelegramHandler;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@EnableScheduling
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationService extends AbstractTelegramHandler {

    // 5 минут
    final long updatePeriod = 300000;

    // 1 минута
//    final long updatePeriod = 60000;
//    final long updatePeriod = 5000;

    @Scheduled(fixedRate = updatePeriod)
    @Async
    public void checkNotification() {
        List<NotificationServiceSettings> notificationServiceSettingsList = notificationServiceSettingsRepository.findAllByActiveIsTrueAndCountOfNotificationPerDayGreaterThan(0);
        LocalDateTime currentDate = LocalDateTime.now();

        notificationServiceSettingsList.forEach(serviceSettings -> {
            LocalDateTime lastNotification = serviceSettings.getLastNotification();

            long differenceBetweenNotifications = SECONDS.between(lastNotification, currentDate);

            if (differenceBetweenNotifications >= serviceSettings.getNotificationInterval()) {
                TelegramUser user = serviceSettings.getUser();
                Long chatId = Long.valueOf(user.getId());
                sendTextMessageForecastAboutFollowingCities(chatId, user, false);

                serviceSettings.setLastNotification(currentDate);
                notificationServiceSettingsRepository.save(serviceSettings);
            }
        });
    }

}
