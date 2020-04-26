package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramChat {

    @Id
    Long id;

    LocalDateTime creationDate;
    Boolean userChat;
    Boolean groupChat;
    Boolean channelChat;
    Boolean superGroupChat;

    @OneToOne
    TelegramUser user;
}
