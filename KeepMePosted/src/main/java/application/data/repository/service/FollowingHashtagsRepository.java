package application.data.repository.service;

import application.data.model.telegram.TelegramUser;
import application.data.model.service.FollowingHashtags;
import org.springframework.data.repository.CrudRepository;

public interface FollowingHashtagsRepository extends CrudRepository<FollowingHashtags, Long> {

    FollowingHashtags findByHashtagAndTelegramUser(String hashtag, TelegramUser user);

}
