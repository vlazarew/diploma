package application.data.repository.twitter;

import application.data.model.telegram.TelegramUser;
import application.data.model.twitter.TwitterPeople;
import org.springframework.data.repository.CrudRepository;

public interface TwitterPeopleRepository extends CrudRepository<TwitterPeople, Long> {
//    TwitterPeople findByNicknameAndTelegramUser(String nickname, TelegramUser telegramUser);
    TwitterPeople findByNickname(String nickname);
}
