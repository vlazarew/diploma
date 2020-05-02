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
@Builder
public class TelegramMessage extends AbstractTelegramEntity {

    @Id
    Integer id;

    String text;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramContact contact;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramUser from;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramLocation location;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramChat chat;
}
