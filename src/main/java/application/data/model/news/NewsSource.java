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
@Table(indexes = {@Index(columnList = "source_name", name = "news_source_source_name_index"),
        @Index(columnList = "name", name = "news_source_name_index")})
public class NewsSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "source_name")
    String sourceName;

    @Column(name = "name")
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
