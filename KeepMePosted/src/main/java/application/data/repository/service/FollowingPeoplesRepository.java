package application.data.repository.service;

import application.data.model.telegram.TelegramUser;
import application.data.model.service.FollowingPeoples;
import org.springframework.data.repository.CrudRepository;

public interface FollowingPeoplesRepository extends CrudRepository<FollowingPeoples, Long> {
    FollowingPeoples findByNicknameAndTelegramUser(String nickname, TelegramUser telegramUser);
}
