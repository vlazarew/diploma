package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TelegramMessage {

    @Id
    Integer id;

    LocalDateTime creationDate;
    String text;

    @OneToOne
    TelegramContact contact;

    @OneToOne
    TelegramUser from;

    @OneToOne
    TelegramChat chat;
}
