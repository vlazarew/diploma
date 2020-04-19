package application.data.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sun.security.util.Password;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Person {

    @Id
    Integer id;

    LocalDateTime creationDate;
    String name;
    String firstName;
    String lastName;
    String phone;
    String email;
    String password;

    String authCode;

}
