package application.data.model.service;

import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NotificationServiceSettings {

    @Id
    @GeneratedValue
    Long id;

    Boolean active = false;

    @Min(0)
    Integer countOfNotificationPerDay;

    @Min(0)
    Float notificationInterval;

    LocalDateTime lastNotification;
    WebService service;

    @OneToMany
    List<WeatherSettings> weatherSettings;

    @ManyToOne
    TelegramUser user;

    @PrePersist
    public void toCreate() {
        countOfNotificationPerDay = 2;
        notificationInterval = (float) (12 * 60 * 60);
        lastNotification = LocalDateTime.now();
    }

}
