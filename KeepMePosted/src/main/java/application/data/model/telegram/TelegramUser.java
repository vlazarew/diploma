package application.data.model.telegram;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TelegramUser {

    @Id
    Integer id;

    LocalDateTime creationDate;
    String userName;
    Boolean bot;
    Boolean registered;
    String firstName;
    String lastName;
    String languageCode;
    String phone;
    String email;
    UserStatus status;

    @OneToOne
    TelegramLocation location;

    // id чата в самой телеге
//    private Long chatId;
    // ид состояния пользователя
//    private Integer stateId;

//    private Boolean isAdmin = false;
//    private Boolean notified = false;

//    @Autowired
//    public User(Long chatId, Integer stateId) {
//        this.chatId = chatId;
//        this.stateId = stateId;
//    }
//
//    public User(Long chatId, Integer stateId, Boolean isAdmin) {
//        this.chatId = chatId;
//        this.stateId = stateId;
//        this.isAdmin = isAdmin;
//    }
}
