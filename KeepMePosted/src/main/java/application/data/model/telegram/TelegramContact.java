package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramContact extends AbstractTelegramEntity {

    @Id
    Integer id;

    String phoneNumber;
    String firstName;
    String lastName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramUser user;
}
