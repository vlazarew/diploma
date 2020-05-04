package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class TelegramContact extends AbstractTelegramEntity {

    @Id
    Integer id;

    String phoneNumber;
    String firstName;
    String lastName;

    @OneToOne
    TelegramUser user;
}
