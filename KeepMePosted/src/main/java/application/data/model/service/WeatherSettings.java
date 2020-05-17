package application.data.model.service;

import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherSettings {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    String city;
    float longitude;
    float latitude;

    @ManyToOne
    TelegramUser user;

    @ManyToOne
    NotificationServiceSettings notificationServiceSettings;
}
