package application.data.model.twitter;

import application.data.model.telegram.TelegramUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class TwitterHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDateTime creationDate;

    String hashtag;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }
}
