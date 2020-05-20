package application.utils.handler;

import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.TwitterSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.model.service.FollowingHashtags;
import application.data.model.service.FollowingPeoples;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class TwitterTelegramMessageHandler extends TelegramHandler {


    final WebService webService = WebService.TwitterService;

    @Override
    @Async
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
        if (!hasText) {
            return;
        }

        TelegramMessage telegramMessage = telegramUpdate.getMessage();
        Long chatId = telegramMessage.getChat().getId();
        TelegramUser telegramUser = telegramMessage.getFrom();
        UserStatus status = telegramUser.getStatus();
        String userAnswer = telegramMessage.getText();

        if (userAnswer.equals(SETTINGS_BACK_BUTTON) && status == UserStatus.TwitterSettings) {
            sendSettingsKeyboard(chatId, "Настройки бота", UserStatus.Settings);
        } else if (userAnswer.equals(ACTIVATE_TWITTER_BUTTON)) {
            saveServiceSettings(telegramUser, true, webService);
            sendTwitterSettingsMessage(chatId, telegramUser, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_TWITTER_BUTTON)) {
            saveServiceSettings(telegramUser, false, webService);
            sendTwitterSettingsMessage(chatId, telegramUser, "Оповещения выключены", null);
        } else if (userAnswer.equals(ADD_PEOPLES_BUTTON)) {
            String listOfPeoples = listTwitterSettingToUser(telegramUser, webService, true);
            String messageToUser = listOfPeoples + "\r\n\r\n" + "Введите никнейм добавляемого пользователя";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddPeople);
        } else if (userAnswer.equals(REMOVE_PEOPLES_BUTTON)) {
            String listOfPeoples = listTwitterSettingToUser(telegramUser, webService, true);
            String messageToUser = listOfPeoples + "\r\n\r\n" + "Введите никнейм удаляемого пользователя";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemovePeople);
        } else if (userAnswer.equals(LIST_FOLLOWING_PEOPLES_BUTTON)) {
            String listOfPeoples = listTwitterSettingToUser(telegramUser, webService, true);
            sendTwitterSettingsMessage(chatId, telegramUser, listOfPeoples, null);
        } else if (userAnswer.equals(ADD_HASHTAG_BUTTON)) {
            String listOfHashtags = listTwitterSettingToUser(telegramUser, webService, false);
            String messageToUser = listOfHashtags + "\r\n\r\n" + "Введите добавляемый хэштег";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddHashtag);
        } else if (userAnswer.equals(REMOVE_HASHTAG_BUTTON)) {
            String listOfHashtags = listTwitterSettingToUser(telegramUser, webService, false);
            String messageToUser = listOfHashtags + "\r\n\r\n" + "Введите удаляемый хэштег";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveHashtag);
        } else if (userAnswer.equals(LIST_FOLLOWING_HASHTAGS_BUTTON)) {
            String listOfHashtags = listTwitterSettingToUser(telegramUser, webService, false);
            sendTwitterSettingsMessage(chatId, telegramUser, listOfHashtags, null);
        } else if (userAnswer.equals(CANCEL_BUTTON) && (status == UserStatus.AddHashtag || status == UserStatus.AddPeople
                || status == UserStatus.RemoveHashtag || status == UserStatus.RemovePeople)) {
            String messageToUser = (status == UserStatus.AddHashtag || status == UserStatus.AddPeople)
                    ? "Добавление отменено" : "Удаление отменено";

            sendTwitterSettingsMessage(chatId, telegramUser, messageToUser, UserStatus.TwitterSettings);
        } else if (status == UserStatus.AddHashtag || status == UserStatus.RemoveHashtag) {
            sendAddRemoveHashtagMessageToUser(chatId, telegramUser, userAnswer, status);
        } else if (status == UserStatus.AddPeople || status == UserStatus.RemovePeople) {
            sendAddRemovePeopleMessageToUser(chatId, telegramUser, userAnswer, status);
        } else if (userAnswer.equals(SHOW_FOLLOWING_TWEETS)) {
            sendTextMessageLastTweets(chatId, telegramUser, true);
        } else if (userAnswer.equals(SHOW_MOST_POPULAR_TWEETS)) {
            sendTextMessageLastTweets(chatId, telegramUser, false);
        }
    }

    private String listTwitterSettingToUser(TelegramUser user, WebService webService, boolean isCheckPeoples) {
        StringBuilder stringBuilder = new StringBuilder();
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        AtomicInteger count = new AtomicInteger();

        if (twitterSettings == null) {
            return "Список пуст";
        } else {
            if (isCheckPeoples) {
                Set<FollowingPeoples> followingPeoples = twitterSettings.getFollowingPeoples();
                String headerMessage = followingPeoples.isEmpty() ? "Список отслеживаемых людей пуст" :
                        "Список отслеживаемых людей: " + "\r\n\r\n";

                stringBuilder.append(headerMessage);

                followingPeoples.forEach(followingPeople -> {
                    count.getAndIncrement();
                    stringBuilder.append(count).append(". ").append(followingPeople.getNickname()).append("\r\n");
                });
            } else {
                Set<FollowingHashtags> followingHashtags = twitterSettings.getFollowingHashtags();
                String headerMessage = followingHashtags.isEmpty() ? "Список отслеживаемых хэштегов пуст" :
                        "Список отслеживаемых хэштегов: " + "\r\n\r\n";

                stringBuilder.append(headerMessage);

                followingHashtags.forEach(followingHashtag -> {
                    count.getAndIncrement();
                    stringBuilder.append(count).append(". ").append(followingHashtag.getHashtag()).append("\r\n");
                });
            }

            return stringBuilder.toString();
        }
    }

    private void sendAddRemoveHashtagMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddHashtag) {
            messageToUser = addHashtagSettingsToUser(user, WebService.TwitterService, userAnswer)
                    ? "Хэштег " + userAnswer + " добавлен в список отслеживаемых"
                    : "Хэштег " + userAnswer + " уже отслеживается вами";
        } else if (status == UserStatus.RemoveHashtag) {
            messageToUser = removeHashtagSettingsToUser(user, WebService.TwitterService, userAnswer)
                    ? "Хэштег " + userAnswer + " удален из списка отслеживаемых"
                    : "Хэштег " + userAnswer + " не отслеживался вами";
        } else {
            return;
        }

        sendTwitterSettingsMessage(chatId, user, messageToUser, UserStatus.TwitterSettings);
    }

    @Transactional
    boolean addHashtagSettingsToUser(TelegramUser user, WebService webService, String hashtag) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        if (twitterSettings != null) {
            Set<FollowingHashtags> followingHashtags = twitterSettings.getFollowingHashtags();

            if (followingHashtags.contains(hashtag)) {
                return false;
            }

            addHashtagToSettings(user, hashtag, twitterSettings, followingHashtags);

            return true;
        }

        TwitterSettings newTwitterSettings = new TwitterSettings();
        newTwitterSettings.setNotificationServiceSettings(notificationServiceSettings);
        newTwitterSettings.setUser(user);

        Set<FollowingHashtags> followingHashtags = new HashSet<>();
        addHashtagToSettings(user, hashtag, newTwitterSettings, followingHashtags);

        return true;

    }

    private void addHashtagToSettings(TelegramUser user, String hashtag, TwitterSettings twitterSettings,
                                      Set<FollowingHashtags> followingHashtags) {
        FollowingHashtags newFollowingHashtag = getSavedFollowingHashtag(user, hashtag);
        followingHashtags.add(newFollowingHashtag);
        twitterSettings.setFollowingHashtags(followingHashtags);

        twitterSettingsRepository.save(twitterSettings);
    }

    private FollowingHashtags getSavedFollowingHashtag(TelegramUser user, String hashtag) {
        FollowingHashtags newFollowingHashtag = new FollowingHashtags();
        newFollowingHashtag.setHashtag(hashtag);
        newFollowingHashtag.setTelegramUser(user);

        followingHashtagsRepository.save(newFollowingHashtag);
        return newFollowingHashtag;
    }

    @Transactional
    boolean removeHashtagSettingsToUser(TelegramUser user, WebService webService, String hashtag) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        if (twitterSettings != null) {
            FollowingHashtags followingHashtag = followingHashtagsRepository.findByHashtagAndTelegramUser(hashtag, user);
            if (followingHashtag != null) {
                Set<FollowingHashtags> twitterSettingsFollowingHashtags = twitterSettings.getFollowingHashtags();
                twitterSettingsFollowingHashtags.remove(followingHashtag);

                twitterSettingsRepository.save(twitterSettings);
                followingHashtagsRepository.delete(followingHashtag);
                return true;
            }
        }

        return false;
    }

    private void sendAddRemovePeopleMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddPeople) {
            messageToUser = addPeopleSettingsToUser(user, WebService.TwitterService, userAnswer)
                    ? "Пользователь " + userAnswer + " добавлен в список отслеживаемых"
                    : "Пользователь " + userAnswer + " уже отслеживается вами";
        } else if (status == UserStatus.RemovePeople) {
            messageToUser = removePeopleSettingsToUser(user, WebService.TwitterService, userAnswer)
                    ? "Пользователь " + userAnswer + " удален из списка отслеживаемых"
                    : "Пользователь " + userAnswer + " не отслеживался вами";
        } else {
            return;
        }

        sendTwitterSettingsMessage(chatId, user, messageToUser, UserStatus.TwitterSettings);
    }

    @Transactional
    boolean addPeopleSettingsToUser(TelegramUser user, WebService webService, String nickname) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        if (twitterSettings != null) {
            Set<FollowingPeoples> followingPeoples = twitterSettings.getFollowingPeoples();

            if (followingPeoples.contains(nickname)) {
                return false;
            }

            addPeopleToSettings(user, nickname, twitterSettings, followingPeoples);

            return true;
        }

        TwitterSettings newTwitterSettings = new TwitterSettings();
        newTwitterSettings.setNotificationServiceSettings(notificationServiceSettings);
        newTwitterSettings.setUser(user);

        Set<FollowingPeoples> followingPeoples = new HashSet<>();
        addPeopleToSettings(user, nickname, newTwitterSettings, followingPeoples);

        return true;

    }

    private void addPeopleToSettings(TelegramUser user, String nickname, TwitterSettings twitterSettings,
                                     Set<FollowingPeoples> followingPeoples) {
        FollowingPeoples newFollowingPeople = getSavedFollowingPeople(user, nickname);
        followingPeoples.add(newFollowingPeople);
        twitterSettings.setFollowingPeoples(followingPeoples);

        twitterSettingsRepository.save(twitterSettings);
    }

    private FollowingPeoples getSavedFollowingPeople(TelegramUser user, String nickname) {
        FollowingPeoples newFollowingPeople = new FollowingPeoples();
        newFollowingPeople.setNickname(nickname);
        newFollowingPeople.setTelegramUser(user);

        followingPeoplesRepository.save(newFollowingPeople);
        return newFollowingPeople;
    }

    @Transactional
    boolean removePeopleSettingsToUser(TelegramUser user, WebService webService, String nickname) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        if (twitterSettings != null) {
            FollowingPeoples followingPeoples = followingPeoplesRepository.findByNicknameAndTelegramUser(nickname, user);
            followingPeoplesRepository.delete(followingPeoples);
            return true;
        }

        return false;
    }
}
