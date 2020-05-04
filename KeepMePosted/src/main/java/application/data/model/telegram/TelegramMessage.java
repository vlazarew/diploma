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
public class TelegramMessage extends AbstractTelegramEntity {

    @Id
    Integer id;

    String text;

    @OneToOne
    TelegramContact contact;

    @OneToOne
    TelegramUser from;

    @OneToOne
    TelegramLocation location;

    @OneToOne
    TelegramChat chat;
}
