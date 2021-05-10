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
@Table(indexes = {@Index(columnList = "chat_id", name = "message_chat_id_index"),
        @Index(columnList = "user_id", name = "message_user_id_index")})
public class TelegramMessage extends AbstractTelegramEntity {

    @Id
    Integer id;

    String text;

    @OneToOne
    TelegramContact contact;

    @OneToOne
    @JoinColumn(name = "user_id")
    TelegramUser from;

    @OneToOne
    TelegramLocation location;

    @OneToOne
    @JoinColumn(name = "chat_id")
    TelegramChat chat;
}
