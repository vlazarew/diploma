package application.data.model.service;

import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSettings {

    @Id
    @GeneratedValue
    Long id;

    Boolean active = false;
    Integer countOfNotification;
    WebService service;


    @OneToMany
    List<WeatherSettings> weatherSettings;

    @ManyToOne
    TelegramUser user;


}
