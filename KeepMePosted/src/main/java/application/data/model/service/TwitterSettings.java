package application.data.model.service;

import application.data.model.news.NewsItem;
import application.data.model.telegram.TelegramUser;
import application.data.model.twitter.Tweet;
import application.data.model.twitter.TwitterHashtag;
import application.data.model.twitter.TwitterPeople;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "twitter_settings_user_id_index")})
public class TwitterSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Date lastTweetCreationDate;

    boolean isActiveUserSettings = true;

    @ManyToOne
    Tweet lastViewedTweet;

    @OneToMany(fetch = FetchType.EAGER)
    @OrderBy("createdAt asc")
    Set<Tweet> viewedTweets;

    @OneToMany(fetch = FetchType.EAGER)
    Set<TwitterPeople> twitterPeople;

    @OneToMany(fetch = FetchType.EAGER)
    Set<TwitterHashtag> twitterHashtags;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @OneToOne
    NotificationServiceSettings notificationServiceSettings;

}
