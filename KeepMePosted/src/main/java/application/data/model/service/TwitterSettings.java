package application.data.model.service;

import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(fetch = FetchType.EAGER)
    Set<FollowingPeoples> followingPeoples;

    @OneToMany(fetch = FetchType.EAGER)
    Set<FollowingHashtags> followingHashtags;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @OneToOne
    NotificationServiceSettings notificationServiceSettings;

}
