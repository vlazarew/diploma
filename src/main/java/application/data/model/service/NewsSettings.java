package application.data.model.service;

import application.data.model.news.NewsCategory;
import application.data.model.news.NewsItem;
import application.data.model.news.NewsSource;
import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "news_settings_user_id_index")})
public class NewsSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Date lastNewsPublicationDate;

    boolean isActiveUserSettings = true;

    @ManyToOne
    NewsItem lastViewedNewsItem;

    @OneToMany(fetch = FetchType.EAGER)
    @OrderBy("publicationDate asc")
    Set<NewsItem> viewedNews;

    @OneToMany(fetch = FetchType.EAGER)
    Set<NewsCategory> newsCategories;

    @OneToMany(fetch = FetchType.EAGER)
    Set<NewsSource> newsSources;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @ManyToOne
    NotificationServiceSettings notificationServiceSettings;
}
