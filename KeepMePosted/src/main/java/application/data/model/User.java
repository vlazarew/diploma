package application.data.model;

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
public class User {

    @Id
    Integer id;

    LocalDateTime creationDate;
    String userName;
    Boolean bot;
    String firstName;
    String lastName;
    String languageCode;
    String phone;
    String email;

    @ManyToOne
    Person person;

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
