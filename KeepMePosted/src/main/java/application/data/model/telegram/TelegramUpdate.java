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
public class TelegramUpdate extends AbstractTelegramEntity {

    @Id
    Integer id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    TelegramMessage message;

}
