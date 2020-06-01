package application.data.model.service;

import application.data.model.YandexWeather.WeatherCity;
import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "weather_settings_user_id_index")})
public class WeatherSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToMany(fetch = FetchType.EAGER)
    Set<WeatherCity> cities;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @ManyToOne
    NotificationServiceSettings notificationServiceSettings;
}
