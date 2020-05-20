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
@Table(indexes = {@Index(columnList = "user_id", name = "notification_settings_user_id_index"),
        @Index(columnList = "service_id", name = "notification_settings_service_id_index"),
        @Index(columnList = "active", name = "notification_settings_active_index"),
        @Index(columnList = "count_of_notification_per_day", name = "notification_settings_count_of_notification_per_day_index")})
public class NotificationServiceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "active")
    Boolean active = false;

    @Min(0)
    @Column(name = "count_of_notification_per_day")
    Integer countOfNotificationPerDay;

    @Min(0)
    Float notificationInterval;

    LocalDateTime lastNotification;
    @Column(name = "service_id")
    WebService service;

    @OneToMany
    List<WeatherSettings> weatherSettings;

    @OneToMany
    List<NewsSettings> newsSettings;

    @OneToMany
    List<TwitterSettings> twitterSettings;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @PrePersist
    public void toCreate() {
        countOfNotificationPerDay = 2;
        notificationInterval = (float) (12 * 60 * 60);
        lastNotification = LocalDateTime.now();
    }

}
