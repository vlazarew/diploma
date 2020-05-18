package application.data.model.service;

import application.data.model.news.NewsCategory;
import application.data.model.telegram.TelegramUser;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(columnList = "user_id", name = "weather_settings_user_id_index")})
public class NewsSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String keyword;

    @OneToOne
    NewsCategory newsCategory;

    @ManyToOne
    @JoinColumn(name = "user_id")
    TelegramUser user;

    @ManyToOne
    NotificationServiceSettings notificationServiceSettings;
}
