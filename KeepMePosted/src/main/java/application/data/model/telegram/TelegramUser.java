package application.data.model.telegram;

import application.data.model.YandexWeather.YandexWeatherTZInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class TelegramUser extends AbstractTelegramEntity {

    @Id
    Integer id;

    String userName;
    Boolean bot;
    Boolean registered = false;
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
