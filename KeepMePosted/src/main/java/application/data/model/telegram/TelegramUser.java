package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TelegramUser {

    @Id
    Integer id;

    LocalDateTime creationDate;
    String userName;
    Boolean bot;
    Boolean registered;
    String firstName;
    String lastName;
    String languageCode;
    String phone;
    String email;
    UserStatus status;

    @OneToOne
    YandexWeatherTZInfo tzInfo;

    @OneToOne
    TelegramLocation location;
}
