package application.data.repository.twitter;

import application.data.model.twitter.Tweet;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TweetRepository extends CrudRepository<Tweet, Long> {

    List<Tweet> findTop10ByNicknameInOrHashtagInOrderByCreatedAtDesc(Collection<String> nickname,
                                                                     Collection<String> hashtag);

    List<Tweet> findTop10ByCreatedAtAfterOrderByRetweetCountDescFavoriteCountDescCreatedAtDesc(Date createdAt);

}
