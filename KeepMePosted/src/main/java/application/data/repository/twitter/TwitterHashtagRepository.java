package application.data.repository.twitter;

import application.data.model.telegram.TelegramUser;
import application.data.model.twitter.TwitterHashtag;
import org.springframework.data.repository.CrudRepository;

public interface TwitterHashtagRepository extends CrudRepository<TwitterHashtag, Long> {

//    TwitterHashtag findByHashtagAndTelegramUser(String hashtag, TelegramUser user);
    TwitterHashtag findByHashtag(String hashtag);

}
