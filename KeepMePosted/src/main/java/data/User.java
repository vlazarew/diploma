package data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    // id чата в самой телеге
    private Long chatId;
    // ид состояния пользователя
    private Integer stateId;

    private String phone;
    private String email;
    private Boolean isAdmin = false;
    private Boolean notified = false;

    public User(Long chatId, Integer stateId) {
        this.chatId = chatId;
        this.stateId = stateId;
    }

    public User(Long chatId, Integer stateId, Boolean isAdmin) {
        this.chatId = chatId;
        this.stateId = stateId;
        this.isAdmin = isAdmin;
    }
}
