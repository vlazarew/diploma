package application.data.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    User user;
}
