package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "contact_user_id_index")})
public class TelegramContact extends AbstractTelegramEntity {

    @Id
    Integer id;

    String phoneNumber;
    String firstName;
    String lastName;

    @OneToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;
}
