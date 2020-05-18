package application.data.model.news;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "uri", name = "news_item_uri_index")})
public class NewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDateTime creationDate;

    String title;
    String link;

    @Column(length = 1024)
    String description;

    String author;

    @Column(name = "uri")
    String uri;

    Date publicationDate;

    String photoUrl;

    @Min(value = 0)
    Integer countOfViewers;

    @OneToOne
    NewsSource source;

    @ManyToMany(fetch = FetchType.EAGER)
    List<NewsCategory> categoryList;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
        setCountOfViewers(0);
    }

}
