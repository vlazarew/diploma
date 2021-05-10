package application.data.model.twitter;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Tweet {

    @Id
    Long id;

    String idStr;

    @Column(length = 300)
    String text;

    Date createdAt;

    String fromUser;
    Long fromUserId;
    String profileImageUrl;
    Long toUserId;

    Integer retweetCount;
    Integer favoriteCount;

    @OneToOne
    TwitterHashtag hashtag;

    @OneToOne
    TwitterPeople nickname;

    String link;

    @PrePersist
    public void toCreate() {
        setLink("twitter.com/" + getFromUser() + "/status/" + getIdStr());
    }

}
