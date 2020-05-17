package application.data.model.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewsItem {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    LocalDateTime creationDate;

    String title;
    String link;

    @Column(length = 1024)
    String description;

    String author;
    String uri;

    Date publicationDate;

    String photoUrl;

    @OneToOne
    NewsSource source;

    @ManyToMany
    List<NewsCategory> categoryList;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }

}
