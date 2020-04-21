package application.data.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramLocation {

    @Id
    @GeneratedValue
    Long id;

    LocalDateTime creationDate;

    float longitude;
    float latitude;
    String city;

    @OneToOne
    TelegramUser user;
}
