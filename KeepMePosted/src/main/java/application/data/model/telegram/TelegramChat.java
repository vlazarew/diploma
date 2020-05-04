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
public class TelegramChat extends AbstractTelegramEntity {

    @Id
    Long id;

    Boolean userChat;
    Boolean groupChat;
    Boolean channelChat;
    Boolean superGroupChat;

    @OneToOne
    TelegramUser user;
}
