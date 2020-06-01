package application.utils.handler;

import application.data.model.service.NewsSettings;
import application.data.model.service.NotificationServiceSettings;
import application.data.model.service.TwitterSettings;
import application.data.model.service.WebService;
import application.data.model.telegram.TelegramMessage;
import application.data.model.telegram.TelegramUpdate;
import application.data.model.telegram.TelegramUser;
import application.data.model.telegram.UserStatus;
import application.data.model.twitter.TwitterHashtag;
import application.data.model.twitter.TwitterPeople;
import application.data.repository.twitter.TwitterHashtagRepository;
import application.data.repository.twitter.TwitterPeopleRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAsync
public class TwitterMessageHandler extends TelegramHandler {

    @Autowired
    TwitterPeopleRepository twitterPeopleRepository;

    @Autowired
    TwitterHashtagRepository twitterHashtagRepository;


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

        boolean addingDeletingStatus = (status == UserStatus.AddHashtag || status == UserStatus.RemoveHashtag ||
                status == UserStatus.AddPeople || status == UserStatus.RemovePeople);

        boolean isTwitterStatus = (status == UserStatus.TwitterMainPage || status == UserStatus.TwitterWatch);

        if (userAnswer.equals(BACK_BUTTON)) {
            backButtonHandler(chatId, telegramUser, status);
        } else if (userAnswer.equals(TWITTER_BUTTON)) {
            sendNewsTwitterMainPageKeyboard(chatId, telegramUser, "Раздел Twitter", UserStatus.TwitterMainPage);
        } else if (userAnswer.equals(ACTIVATE_TWITTER_BUTTON) & status == UserStatus.TwitterSettings) {
            saveServiceSettings(telegramUser, true, webService);
            sendTwitterSettingsMessage(chatId, telegramUser, "Оповещения включены", null);
        } else if (userAnswer.equals(DEACTIVATE_TWITTER_BUTTON) & status == UserStatus.TwitterSettings) {
            saveServiceSettings(telegramUser, false, webService);
            sendTwitterSettingsMessage(chatId, telegramUser, "Оповещения выключены", null);
        } else if (userAnswer.equals(LIST_FOLLOWING_PEOPLES_BUTTON)) {
            String messageToUser = listTwitterPeoplesToUser(telegramUser);
            sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.PeoplesList);
        } else if (userAnswer.equals(LIST_FOLLOWING_HASHTAGS_BUTTON)) {
            String messageToUser = listTwitterHashtagsToUser(telegramUser);
            sendCommonAddDeleteKeyboard(chatId, messageToUser, UserStatus.HashtagsList);
        } else if (status == UserStatus.PeoplesList) {
            peoplesListHandler(chatId, telegramUser, userAnswer);
        } else if (status == UserStatus.HashtagsList) {
            hashtagsListHandler(chatId, telegramUser, userAnswer);
        } else if (userAnswer.equals(CANCEL_BUTTON) && addingDeletingStatus) {
            String messageToUser = getMessageToUser(status);
            sendTwitterSettingsMessage(chatId, telegramUser, messageToUser, UserStatus.TwitterSettings);
        } else if (addingDeletingStatus) {
            sendAddRemoveMessageToUser(chatId, telegramUser, userAnswer, status);
        } else if (((userAnswer.equals(WATCH_BUTTON) || userAnswer.equals(NEXT_ITEM_BUTTON)) & isTwitterStatus)) {
            sendTwitterWatchKeyboard(chatId, telegramUser, UserStatus.TwitterWatch, true);
        } else if (userAnswer.equals(PREVIOUS_ITEM_BUTTON) & isTwitterStatus) {
            sendTwitterWatchKeyboard(chatId, telegramUser, UserStatus.TwitterWatch, false);
        } else if (userAnswer.equals(EXIT_WATCH_BUTTON) & isTwitterStatus) {
            sendNewsTwitterMainPageKeyboard(chatId, telegramUser, "Раздел Twitter", UserStatus.TwitterMainPage);
        } else if (userAnswer.equals(FIRST_ITEM_BUTTON) & isTwitterStatus) {
            clearUserNewsHistory(telegramUser, chatId);
        } else if (userAnswer.equals(ACTIVATE_PERSON_SETTINGS) || userAnswer.equals(DEACTIVATE_PERSON_SETTINGS) & isTwitterStatus) {
            changeActivityForNews(telegramUser, chatId);
        }
    }

    private String getMessageToUser(UserStatus status) {
        String messageToUser = "";
        if (status == UserStatus.AddPeople) {
            messageToUser = "Добавление пользователя отменено";
        } else if (status == UserStatus.RemovePeople) {
            messageToUser = "Удаление пользователя отменено";
        } else if (status == UserStatus.AddHashtag) {
            messageToUser = "Добавление хэштега отменено";
        } else if (status == UserStatus.RemoveHashtag) {
            messageToUser = "Удаление хэштега отменено";
        }
        return messageToUser;
    }

    @Transactional
    void changeActivityForNews(TelegramUser telegramUser, Long chatId) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(telegramUser.getId());
        if (twitterSettings == null) {
            return;
        }

        twitterSettings.setActiveUserSettings(!twitterSettings.isActiveUserSettings());
        twitterSettingsRepository.save(twitterSettings);
        clearUserNewsHistory(telegramUser, chatId);
    }

    private void clearUserNewsHistory(TelegramUser telegramUser, Long chatId) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(telegramUser.getId());
        if (twitterSettings == null) {
            return;
        }

        twitterSettings.setLastViewedTweet(null);
        twitterSettings.setLastTweetCreationDate(new Date());
        twitterSettings.setViewedTweets(new HashSet<>());
        twitterSettingsRepository.save(twitterSettings);

        sendTwitterWatchKeyboard(chatId, telegramUser, UserStatus.TwitterWatch, true);
    }

    private void sendAddRemoveMessageToUser(Long chatId, TelegramUser user, String userAnswer, UserStatus status) {
        String messageToUser;
        if (status == UserStatus.AddPeople) {
            messageToUser = addTwitterPeopleToUser(user, userAnswer)
                    ? "Пользователь *" + userAnswer + "* добавлен в список отслеживаемых"
                    : "Пользователь *" + userAnswer + "* уже отслеживается вами";
        } else if (status == UserStatus.RemovePeople) {
            messageToUser = removeTwitterPeopleToUser(user, userAnswer)
                    ? "Пользователь *" + userAnswer + "* удален из списка отслеживаемых"
                    : "Пользователь *" + userAnswer + "* не отслеживается вами";
        } else if (status == UserStatus.AddHashtag) {
            messageToUser = addHashtagSettingsToUser(user, userAnswer)
                    ? "Хэштег *" + userAnswer + "* добавлен в список отслеживаемых"
                    : "Хэштег *" + userAnswer + "* не отслеживается вами";
        } else if (status == UserStatus.RemoveHashtag) {
            messageToUser = removeTwitterHashtagToUser(user, userAnswer)
                    ? "Хэштег *" + userAnswer + "* удален из списка отслеживаемых"
                    : "Хэштег *" + userAnswer + "* не отслеживается вами";
        } else {
            return;
        }

        UserStatus nextStatus = (status == UserStatus.AddPeople || status == UserStatus.RemovePeople) ? UserStatus.PeoplesList
                : UserStatus.HashtagsList;

        sendCommonAddDeleteKeyboard(chatId, messageToUser, nextStatus);
    }

    private void backButtonHandler(Long chatId, TelegramUser telegramUser, UserStatus status) {
        if (status == UserStatus.TwitterMainPage) {
            sendMessageToUserByCustomMainKeyboard(chatId, telegramUser, "Главная страница", UserStatus.MainPage);
        } else if (status == UserStatus.TwitterSettings) {
            sendCommonSettingKeyboard(chatId, "Настройки Twitter", UserStatus.TwitterCommonSettings);
        } else if (status == UserStatus.HashtagsList || status == UserStatus.PeoplesList) {
            sendTwitterSettingsMessage(chatId, telegramUser, "Настройки рассылки Tweets", UserStatus.TwitterSettings);
        }
    }

    private String listTwitterPeoplesToUser(TelegramUser user) {
        StringBuilder stringBuilder = new StringBuilder();
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        AtomicInteger count = new AtomicInteger();

        if (twitterSettings == null) {
            return "Список пуст";
        }

        Set<TwitterPeople> twitterPeople = twitterSettings.getTwitterPeople();
        String headerMessage = twitterPeople.isEmpty() ? "Список отслеживаемых людей пуст" :
                "Список отслеживаемых людей: " + "\r\n\r\n";

        stringBuilder.append(headerMessage);

        twitterPeople.forEach(followingPeople -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(followingPeople.getNickname()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    private void peoplesListHandler(Long chatId, TelegramUser telegramUser, String userAnswer) {
        if (userAnswer.equals(COMMON_ADD)) {
            String listOfPeoples = listTwitterPeoplesToUser(telegramUser);
            String messageToUser = listOfPeoples + "\r\n\r\n" + "Введите никнейм добавляемого пользователя";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddPeople);
        } else if (userAnswer.equals(COMMON_DELETE)) {
            String listOfPeoples = listTwitterPeoplesToUser(telegramUser);
            String messageToUser = listOfPeoples + "\r\n\r\n" + "Введите никнейм удаляемого пользователя";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemovePeople);
        }
    }

    private String listTwitterHashtagsToUser(TelegramUser user) {
        StringBuilder stringBuilder = new StringBuilder();
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        AtomicInteger count = new AtomicInteger();

        if (twitterSettings == null) {
            return "Список пуст";
        }

        Set<TwitterHashtag> twitterHashtags = twitterSettings.getTwitterHashtags();
        String headerMessage = twitterHashtags.isEmpty() ? "Список отслеживаемых хэштегов пуст" :
                "Список отслеживаемых хэштегов: " + "\r\n\r\n";

        stringBuilder.append(headerMessage);

        twitterHashtags.forEach(followingHashtag -> {
            count.getAndIncrement();
            stringBuilder.append(count).append(". ").append(followingHashtag.getHashtag()).append("\r\n");
        });

        return stringBuilder.toString();
    }

    private void hashtagsListHandler(Long chatId, TelegramUser telegramUser, String userAnswer) {
        if (userAnswer.equals(COMMON_ADD)) {
            String listOfHashtags = listTwitterHashtagsToUser(telegramUser);
            String messageToUser = listOfHashtags + "\r\n\r\n" + "Введите добавляемый хэштег";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.AddHashtag);
        } else if (userAnswer.equals(COMMON_DELETE)) {
            String listOfHashtags = listTwitterHashtagsToUser(telegramUser);
            String messageToUser = listOfHashtags + "\r\n\r\n" + "Введите удаляемый хэштег";
            sendTextMessageAddDeleteSomething(chatId, messageToUser, UserStatus.RemoveHashtag);
        }
    }

    @Transactional
    boolean addHashtagSettingsToUser(TelegramUser user, String hashtagText) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        boolean needToCreateNewRule = (twitterSettings == null || hashtagText.equals(""));
        if (needToCreateNewRule) {
            saveTwitterHashtag(user, notificationServiceSettings, hashtagText);
        } else {
            Set<TwitterHashtag> hashtags = twitterSettings.getTwitterHashtags();
            TwitterHashtag hashtag = findSaveTwitterHashtag(hashtagText);
            needToCreateNewRule = (!hashtags.contains(hashtag));

            if (needToCreateNewRule) {
                hashtags.add(hashtag);
                twitterSettings.setTwitterHashtags(hashtags);
                twitterSettingsRepository.save(twitterSettings);
            }
        }

        return needToCreateNewRule;
    }

    private void saveTwitterHashtag(TelegramUser user, NotificationServiceSettings notificationServiceSettings,
                                    String hashtagText) {
        TwitterSettings twitterSettings = new TwitterSettings();
        twitterSettings.setUser(user);
        twitterSettings.setNotificationServiceSettings(notificationServiceSettings);

        TwitterHashtag hashtag = findSaveTwitterHashtag(hashtagText);
        Set<TwitterHashtag> twitterHashtags = new HashSet<>();
        twitterHashtags.add(hashtag);

        twitterSettings.setTwitterHashtags(twitterHashtags);
        twitterSettingsRepository.save(twitterSettings);
    }

    @Transactional
    boolean removeTwitterHashtagToUser(TelegramUser user, String hashtagText) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());

        if (twitterSettings == null) {
            return false;
        }

        Set<TwitterHashtag> twitterHashtags = twitterSettings.getTwitterHashtags();
        TwitterHashtag hashtag = twitterHashtagRepository.findByHashtag(hashtagText);

        boolean needToDelete = (twitterHashtags.contains(hashtag));
        if (needToDelete) {
            twitterHashtags.remove(hashtag);
            twitterSettings.setTwitterHashtags(twitterHashtags);
            twitterSettingsRepository.save(twitterSettings);
        }

        return needToDelete;
    }

    @Transactional
    boolean addTwitterPeopleToUser(TelegramUser user, String nickname) {
        NotificationServiceSettings notificationServiceSettings = saveFindServiceSettings(user, webService);

        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        boolean needToCreateNewRule = (twitterSettings == null || nickname.equals(""));
        if (needToCreateNewRule) {
            saveTwitterPeople(user, notificationServiceSettings, nickname);
        } else {
            Set<TwitterPeople> peopleSet = twitterSettings.getTwitterPeople();
            TwitterPeople people = findSaveTwitterPeople(nickname);
            needToCreateNewRule = (!peopleSet.contains(people));

            if (needToCreateNewRule) {
                peopleSet.add(people);
                twitterSettings.setTwitterPeople(peopleSet);
                twitterSettingsRepository.save(twitterSettings);
            }
        }

        return needToCreateNewRule;
    }

    private void saveTwitterPeople(TelegramUser user, NotificationServiceSettings notificationServiceSettings,
                                   String nickname) {
        TwitterSettings twitterSettings = new TwitterSettings();
        twitterSettings.setUser(user);
        twitterSettings.setNotificationServiceSettings(notificationServiceSettings);

        TwitterPeople twitterPeople = findSaveTwitterPeople(nickname);
        Set<TwitterPeople> twitterPeopleSet = new HashSet<>();
        twitterPeopleSet.add(twitterPeople);

        twitterSettings.setTwitterPeople(twitterPeopleSet);
        twitterSettingsRepository.save(twitterSettings);
    }

    @Transactional
    boolean removeTwitterPeopleToUser(TelegramUser user, String nickname) {
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());

        if (twitterSettings == null) {
            return false;
        }

        Set<TwitterPeople> twitterPeopleSet = twitterSettings.getTwitterPeople();
        TwitterPeople twitterPeople = twitterPeopleRepository.findByNickname(nickname);

        boolean needToDelete = (twitterPeopleSet.contains(twitterPeople));
        if (needToDelete) {
            twitterPeopleSet.remove(twitterPeople);
            twitterSettings.setTwitterPeople(twitterPeopleSet);
            twitterSettingsRepository.save(twitterSettings);
        }

        return needToDelete;
    }

    private TwitterHashtag findSaveTwitterHashtag(String hashtag) {
        TwitterHashtag twitterHashtag = twitterHashtagRepository.findByHashtag(hashtag);
        if (twitterHashtag == null) {
            twitterHashtag = new TwitterHashtag();
            twitterHashtag.setHashtag(hashtag);
            twitterHashtagRepository.save(twitterHashtag);
        }

        return twitterHashtag;
    }

    private TwitterPeople findSaveTwitterPeople(String nickname) {
        TwitterPeople twitterPeople = twitterPeopleRepository.findByNickname(nickname);
        if (twitterPeople == null) {
            twitterPeople = new TwitterPeople();
            twitterPeople.setNickname(nickname);
            twitterPeopleRepository.save(twitterPeople);
        }

        return twitterPeople;
    }
}
