package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUser extends AbstractTelegramEntity {

    @Id
    Integer id;

    String userName;
    Boolean bot;
    // false - Активируется блок с регистрацией, true - регистрации не будет
    Boolean registered = true;
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
