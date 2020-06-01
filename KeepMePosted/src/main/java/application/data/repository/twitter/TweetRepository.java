package application.data.repository.twitter;

import application.data.model.twitter.Tweet;
import application.data.model.twitter.TwitterHashtag;
import application.data.model.twitter.TwitterPeople;
import org.springframework.data.repository.CrudRepository;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TweetRepository extends CrudRepository<Tweet, Long> {

//    List<Tweet> findTop10ByNicknameInOrHashtagInOrderByCreatedAtDesc(Collection<String> nickname,
//                                                                     Collection<String> hashtag);

    Tweet findTop1ByCreatedAtBeforeAndNicknameInOrHashtagInOrderByCreatedAtDesc(Date createdAt, Collection<TwitterPeople> nickname, Collection<TwitterHashtag> hashtag);

    //    List<Tweet> findTop10ByCreatedAtAfterOrderByRetweetCountDescFavoriteCountDescCreatedAtDesc(Date createdAt);
    Tweet findTop1ByCreatedAtBeforeOrderByRetweetCountDescFavoriteCountDescCreatedAtDesc(Date createdAt);

}
