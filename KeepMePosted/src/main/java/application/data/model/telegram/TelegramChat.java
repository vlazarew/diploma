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
public class TelegramChat extends AbstractTelegramEntity {

    @Id
    Long id;

    Boolean userChat;
    Boolean groupChat;
    Boolean channelChat;
    Boolean superGroupChat;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramUser user;
}
