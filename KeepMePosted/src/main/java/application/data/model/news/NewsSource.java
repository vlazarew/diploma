package application.data.model.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class NewsSource {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    String name;

    String link;
    String logoImageUrl;

    LocalDateTime creationDate;
    LocalDateTime lastUpdate;


    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }
}
