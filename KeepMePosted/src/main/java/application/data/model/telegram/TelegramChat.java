package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "chat_user_id_index")})
public class TelegramChat extends AbstractTelegramEntity {

    @Id
    Long id;

    Boolean userChat;
    Boolean groupChat;
    Boolean channelChat;
    Boolean superGroupChat;

    @OneToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;
}
