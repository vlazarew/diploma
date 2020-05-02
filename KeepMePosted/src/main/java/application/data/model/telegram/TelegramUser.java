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
@Builder
@Setter
public class TelegramUser extends AbstractTelegramEntity {

    @Id
    Integer id;

    String userName;
    Boolean bot;
    Boolean registered;
    String firstName;
    String lastName;
    String languageCode;
    String phone;
    String email;
    UserStatus status;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    YandexWeatherTZInfo tzInfo;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramLocation location;
}
