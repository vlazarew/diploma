package application.data.model.news;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "name", name = "news_category_name_index")})
@Getter
@Setter
public class NewsCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDateTime creationDate;

    @Column(name = "name")
    String name;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }
}
