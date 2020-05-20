package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "message_id", name = "update_message_id_index")})
public class TelegramUpdate extends AbstractTelegramEntity {

    @Id
    Integer id;

    @OneToOne
    @JoinColumn(name = "message_id")
    TelegramMessage message;

}
