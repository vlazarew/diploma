package application.utils.handler;

import application.data.model.YandexWeather.WeatherCity;
import application.data.model.YandexWeather.YandexWeather;
import application.data.model.YandexWeather.YandexWeatherFact;
import application.data.model.YandexWeather.YandexWeatherTZInfo;
import application.data.model.news.NewsCategory;
import application.data.model.news.NewsItem;
import application.data.model.service.*;
import application.data.model.telegram.*;
import application.data.model.twitter.Tweet;
import application.data.model.twitter.TwitterHashtag;
import application.data.model.twitter.TwitterPeople;
import application.data.repository.YandexWeather.WeatherCityRepository;
import application.data.repository.news.NewsItemRepository;
import application.data.repository.service.NewsSettingsRepository;
import application.data.repository.service.NotificationServiceSettingsRepository;
import application.data.repository.service.TwitterSettingsRepository;
import application.data.repository.service.WeatherSettingsRepository;
import application.data.repository.telegram.TelegramChatRepository;
import application.data.repository.telegram.TelegramLocationRepository;
import application.data.repository.telegram.TelegramUserRepository;
import application.data.repository.twitter.TweetRepository;
import application.data.repository.twitter.TwitterHashtagRepository;
import application.data.repository.twitter.TwitterPeopleRepository;
import application.service.weather.YandexWeatherService;
import application.telegram.TelegramBot;
import application.telegram.TelegramKeyboards;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
@PropertySource("classpath:interface.properties")
@EnableAsync
public class TelegramHandler implements TelegramMessageHandler {

    @Autowired
    TelegramBot telegramBot;
    @Autowired
    public TelegramUserRepository userRepository;
    @Autowired
    public TelegramChatRepository telegramChatRepository;
    @Autowired
    public NotificationServiceSettingsRepository notificationServiceSettingsRepository;
    @Autowired
    public TelegramKeyboards telegramKeyboards;
    @Autowired
    public WeatherSettingsRepository weatherSettingsRepository;
    @Autowired
    public YandexWeatherService yandexWeatherService;
    @Autowired
    public TelegramLocationRepository telegramLocationRepository;
    @Autowired
    public NewsSettingsRepository newsSettingsRepository;
    @Autowired
    public NewsItemRepository newsItemRepository;
    @Autowired
    public TwitterSettingsRepository twitterSettingsRepository;
    @Autowired
    public TwitterHashtagRepository twitterHashtagRepository;
    @Autowired
    public TwitterPeopleRepository twitterPeopleRepository;
    @Autowired
    public TweetRepository tweetRepository;
    @Autowired
    public WeatherCityRepository weatherCityRepository;

    //region Кнопки в приложении
    @Value("${telegram.START_COMMAND}")
    public String START_COMMAND;

    // Базовые кнопки
    @Value("${telegram.HELLO_BUTTON}")
    public String HELLO_BUTTON;
    @Value("${telegram.HELP_BUTTON}")
    public String HELP_BUTTON;

    // Кнопки регистрации
    @Value("${telegram.REGISTER_BUTTON}")
    public String REGISTER_BUTTON;
    @Value("${telegram.CANCEL_REGISTRATION_BUTTON}")
    public String CANCEL_REGISTRATION_BUTTON;
    @Value("${telegram.NEXT_BUTTON}")
    public String NEXT_BUTTON;
    @Value("${telegram.SHARE_PHONE_NUMBER}")
    public String SHARE_PHONE_NUMBER;
    @Value("${telegram.CONFIRM_EMAIL}")
    public String CONFIRM_EMAIL;

    // Кнопки настроек
    @Value("${telegram.SETTINGS_BUTTON}")
    public String SETTINGS_BUTTON;
    @Value("${telegram.NOTIFICATION_SETTINGS_BUTTON}")
    public String NOTIFICATION_SETTINGS_BUTTON;
    @Value("${telegram.BACK_BUTTON}")
    public String BACK_BUTTON;

    // Интеграция с погодой
//    @Value("${telegram.WEATHER_SETTINGS_BUTTON}")
//    public String WEATHER_SETTINGS_BUTTON;
    @Value("${telegram.ACTIVATE_WEATHER_BUTTON}")
    public String ACTIVATE_WEATHER_BUTTON;
    @Value("${telegram.DEACTIVATE_WEATHER_BUTTON}")
    public String DEACTIVATE_WEATHER_BUTTON;
    @Value("${telegram.SHARE_LOCATION_BUTTON}")
    public String SHARE_LOCATION_BUTTON;
    @Value("${telegram.ADD_CITY_WEATHER_BUTTON}")
    public String ADD_CITY_WEATHER_BUTTON;
    @Value("${telegram.REMOVE_CITY_WEATHER_BUTTON}")
    public String REMOVE_CITY_WEATHER_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_CITIES_BUTTON}")
    public String LIST_FOLLOWING_CITIES_BUTTON;
    @Value("${telegram.CANCEL_BUTTON}")
    public String CANCEL_BUTTON;
    @Value("${telegram.WEATHER_IN_CURRENT_LOCATION_BUTTON}")
    public String WEATHER_IN_CURRENT_LOCATION_BUTTON;
//    @Value("${telegram.SHOW_INFO_ABOUT_FOLLOWING_CITIES}")
//    public String SHOW_INFO_ABOUT_FOLLOWING_CITIES;

    //region Интервалы оповещений
    @Value("${telegram.NOTIFICATION_15_MINUTES}")
    public String NOTIFICATION_15_MINUTES;
    @Value("${telegram.NOTIFICATION_30_MINUTES}")
    public String NOTIFICATION_30_MINUTES;
    @Value("${telegram.NOTIFICATION_1_HOUR}")
    public String NOTIFICATION_1_HOUR;
    @Value("${telegram.NOTIFICATION_2_HOURS}")
    public String NOTIFICATION_2_HOURS;
    @Value("${telegram.NOTIFICATION_3_HOURS}")
    public String NOTIFICATION_3_HOURS;
    @Value("${telegram.NOTIFICATION_6_HOURS}")
    public String NOTIFICATION_6_HOURS;
    @Value("${telegram.NOTIFICATION_9_HOURS}")
    public String NOTIFICATION_9_HOURS;
    @Value("${telegram.NOTIFICATION_12_HOURS}")
    public String NOTIFICATION_12_HOURS;
    @Value("${telegram.NOTIFICATION_24_HOURS}")
    public String NOTIFICATION_24_HOURS;
    //endregion

    //region Интеграция с новостями
//    @Value("${telegram.SHOW_FOLLOWING_NEWS}")
//    public String SHOW_FOLLOWING_NEWS;
//    @Value("${telegram.SHOW_ALL_NEWS}")
//    public String SHOW_ALL_NEWS;
    @Value("${telegram.NEWS_SETTINGS_BUTTON}")
    public String NEWS_SETTINGS_BUTTON;
    @Value("${telegram.ACTIVATE_NEWS_BUTTON}")
    public String ACTIVATE_NEWS_BUTTON;
    @Value("${telegram.DEACTIVATE_NEWS_BUTTON}")
    public String DEACTIVATE_NEWS_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_CATEGORIES_BUTTON}")
    public String LIST_FOLLOWING_CATEGORIES_BUTTON;
    @Value("${telegram.ADD_CATEGORY_NEWS_BUTTON}")
    public String ADD_CATEGORY_NEWS_BUTTON;
    @Value("${telegram.REMOVE_CATEGORY_NEWS_BUTTON}")
    public String REMOVE_CATEGORY_NEWS_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_SOURCES_BUTTON}")
    public String LIST_FOLLOWING_SOURCES_BUTTON;
    @Value("${telegram.ADD_SOURCE_NEWS_BUTTON}")
    public String ADD_SOURCE_NEWS_BUTTON;
    @Value("${telegram.REMOVE_SOURCE_NEWS_BUTTON}")
    public String REMOVE_SOURCE_NEWS_BUTTON;

    @Value("${telegram.COMMON_ADD}")
    public String COMMON_ADD;
    @Value("${telegram.COMMON_DELETE}")
    public String COMMON_DELETE;
    @Value("${telegram.COMMON_BANNED_NEWS_SOURCES_ADD}")
    public String COMMON_BANNED_NEWS_SOURCES_ADD;
    @Value("${telegram.COMMON_BANNED_NEWS_SOURCES_DELETE}")
    public String COMMON_BANNED_NEWS_SOURCES_DELETE;
    //endregion

    //region Интеграция с Twitter
    @Value("${telegram.TWITTER_SETTINGS_BUTTON}")
    public String TWITTER_SETTINGS_BUTTON;
    @Value("${telegram.ACTIVATE_TWITTER_BUTTON}")
    public String ACTIVATE_TWITTER_BUTTON;
    @Value("${telegram.DEACTIVATE_TWITTER_BUTTON}")
    public String DEACTIVATE_TWITTER_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_PEOPLES_BUTTON}")
    public String LIST_FOLLOWING_PEOPLES_BUTTON;
    @Value("${telegram.ADD_PEOPLES_BUTTON}")
    public String ADD_PEOPLES_BUTTON;
    @Value("${telegram.REMOVE_PEOPLES_BUTTON}")
    public String REMOVE_PEOPLES_BUTTON;
    @Value("${telegram.LIST_FOLLOWING_HASHTAGS_BUTTON}")
    public String LIST_FOLLOWING_HASHTAGS_BUTTON;
    @Value("${telegram.ADD_HASHTAG_BUTTON}")
    public String ADD_HASHTAG_BUTTON;
    @Value("${telegram.REMOVE_HASHTAG_BUTTON}")
    public String REMOVE_HASHTAG_BUTTON;
//    @Value("${telegram.SHOW_FOLLOWING_TWEETS}")
//    public String SHOW_FOLLOWING_TWEETS;
//    @Value("${telegram.SHOW_MOST_POPULAR_TWEETS}")
//    public String SHOW_MOST_POPULAR_TWEETS;
    //endregion

    //region Новый интерфейс
    @Value("${telegram.NEWS_BUTTON}")
    public String NEWS_BUTTON;
    @Value("${telegram.TWITTER_BUTTON}")
    public String TWITTER_BUTTON;
    @Value("${telegram.WEATHER_BUTTON}")
    public String WEATHER_BUTTON;

    @Value("${telegram.WATCH_BUTTON}")
    public String WATCH_BUTTON;
    @Value("${telegram.COMMON_SETTINGS_BUTTON}")
    public String COMMON_SETTINGS_BUTTON;

    @Value("${telegram.NEXT_ITEM_BUTTON}")
    public String NEXT_ITEM_BUTTON;
    @Value("${telegram.PREVIOUS_ITEM_BUTTON}")
    public String PREVIOUS_ITEM_BUTTON;
    @Value("${telegram.FIRST_ITEM_BUTTON}")
    public String FIRST_ITEM_BUTTON;
    @Value("${telegram.EXIT_WATCH_BUTTON}")
    public String EXIT_WATCH_BUTTON;
    @Value("${telegram.DEACTIVATE_PERSON_SETTINGS}")
    public String DEACTIVATE_PERSON_SETTINGS;
    @Value("${telegram.ACTIVATE_PERSON_SETTINGS}")
    public String ACTIVATE_PERSON_SETTINGS;
    //endregion
    //endregion

    @Override
    public void handle(TelegramUpdate telegramUpdate, boolean hasText, boolean hasContact, boolean hasLocation) {
    }

    //region Send messages
    @Override
    public void sendMessageToUserByCustomMainKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCustomReplyMainKeyboardMarkup(telegramUser);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageReplyKeyboardMarkup(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup,
                                                   UserStatus status) {
        SendMessage sendMessage = makeSendMessage(chatId, text, replyKeyboardMarkup);

        try {
            telegramBot.execute(sendMessage);

            if (status != null) {
                TelegramUser user;
                Optional<TelegramChat> chat = telegramChatRepository.findById(chatId);

                if (chat.isPresent()) {
                    user = chat.get().getUser();

                    user.setStatus(status);
                    userRepository.save(user);
                } else {
                    log.error("Не найден чат с id: " + chatId);
                }
            }

        } catch (TelegramApiException e) {
            log.error(e);
        }

    }

    @Override
    public void sendWeatherSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getWeatherSettingsKeyboard(user,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageForecastAboutFollowingCities(Long chatId, TelegramUser telegramUser, boolean isUserLocation) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(telegramUser.getId());
        Set<WeatherCity> weatherCities = weatherSettings.getCities();
        Date userLastDate = weatherSettings.getLastCityCreationDate();

        weatherSettings.setLastCityCreationDate(null);
        weatherSettingsRepository.save(weatherSettings);

        List<String> textList = new ArrayList<>();
        for (int index = 0; index < weatherCities.size(); index++) {
            textList.add(getCityInfo(telegramUser, false, true));
        }
        textList.forEach(text -> sendTextMessageReplyKeyboardMarkup(chatId, text, null, null));

        weatherSettings.setLastCityCreationDate(userLastDate);
        weatherSettingsRepository.save(weatherSettings);
    }

    @Override
    public void sendNewsSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNewsSettingsKeyboard(user,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void saveServiceSettings(TelegramUser user, boolean active, WebService webService) {
        NotificationServiceSettings notificationServiceSettings = notificationServiceSettingsRepository.findByUserAndService(user, webService)
                .orElseGet(() -> {
                    NotificationServiceSettings newNotificationServiceSettings = new NotificationServiceSettings();
                    newNotificationServiceSettings.setService(webService);
                    newNotificationServiceSettings.setUser(user);
                    return newNotificationServiceSettings;
                });

        notificationServiceSettings.setActive(active);
        notificationServiceSettingsRepository.save(notificationServiceSettings);
    }

    @Override
    public void sendTextMessageAddDeleteSomething(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAddDeleteSomethingKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    public NotificationServiceSettings saveFindServiceSettings(TelegramUser user, WebService webService) {
        return notificationServiceSettingsRepository.findByUserAndService(user, webService)
                .orElseGet(() -> {
                    NotificationServiceSettings newNotificationServiceSettings = new NotificationServiceSettings();
                    newNotificationServiceSettings.setService(webService);
                    newNotificationServiceSettings.setUser(user);

                    return notificationServiceSettingsRepository.save(newNotificationServiceSettings);
                });
    }

    @Override
    public void sendTextMessageLastNews(Long chatId, TelegramUser telegramUser, boolean isFollowingNews) {
        List<String> textList = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            textList.add(getNewsInfo(telegramUser, isFollowingNews));
        }

        textList.forEach(text -> sendTextMessageReplyKeyboardMarkup(chatId, text, null, null));
    }

    @Override
    public void sendTwitterSettingsMessage(Long chatId, TelegramUser user, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getTwitterSettingsKeyboard(user,
                notificationServiceSettingsRepository);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendTextMessageLastTweets(Long chatId, TelegramUser telegramUser, boolean isFollowingTweets) {
        List<String> textList = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            textList.add(getTweetsInfo(telegramUser, isFollowingTweets));
        }

        textList.forEach(text -> sendTextMessageReplyKeyboardMarkup(chatId, text, null, null));
    }

    @Override
    public void sendNewsTwitterMainPageKeyboard(Long chatId, TelegramUser telegramUser, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNewsTwitterMainPageKeyboard();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendCommonSettingKeyboard(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getCommonSettingsKeyboardMarkup();
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendCommonAddDeleteKeyboard(Long chatId, String text, UserStatus status) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getAddDeleteCommonKeyboard(status);
        sendTextMessageReplyKeyboardMarkup(chatId, text, replyKeyboardMarkup, status);
    }

    @Override
    public void sendNewsWatchKeyboard(Long chatId, TelegramUser user, UserStatus status,
                                      boolean isNextNews) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNewsWatchKeyboard(user);
        sendTextMessageReplyKeyboardMarkup(chatId, getNewsInfo(user, isNextNews), replyKeyboardMarkup, status);
    }

    @Override
    public void sendTwitterWatchKeyboard(Long chatId, TelegramUser user, UserStatus status,
                                         boolean isNextNews) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNewsWatchKeyboard(user);
        sendTextMessageReplyKeyboardMarkup(chatId, getTweetsInfo(user, isNextNews), replyKeyboardMarkup, status);
    }

    @Override
    public void sendWeatherWatchKeyboard(Long chatId, TelegramUser user, UserStatus status,
                                         boolean isNextForecast, boolean isUserLocation) {
        ReplyKeyboardMarkup replyKeyboardMarkup = telegramKeyboards.getNewsWatchKeyboard(user);
        sendTextMessageReplyKeyboardMarkup(chatId, getCityInfo(user, isUserLocation, isNextForecast), replyKeyboardMarkup, status);
    }

    //endregion

    //region Help methods
    private SendMessage makeSendMessage(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    private String getCityInfo(TelegramUser telegramUser, boolean isUserLocation, boolean isNextForecast) {
        WeatherSettings weatherSettings = weatherSettingsRepository.findByUserId(telegramUser.getId());
        StringBuilder result = new StringBuilder();

        if (weatherSettings == null) {
            isUserLocation = true;
        }

        if (isUserLocation) {
            TelegramLocation userLocation = telegramUser.getLocation();
            return saveWeatherInfoAndDoMessageToUser(userLocation.getLongitude(),
                    userLocation.getLatitude(), telegramUser, true, userLocation.getCity());
        }

        Set<WeatherCity> weatherCities = weatherSettings.getCities();
        if (weatherCities.isEmpty()) {
            return "Список отслеживаемых городов пуст.";
        }

        WeatherCity weatherCity;
        if (isNextForecast) {
            Date lastCreationDate = weatherSettings.getLastCityCreationDate();
            if (lastCreationDate == null) {
                lastCreationDate = new Date();
            }

            weatherCity = weatherCityRepository.findTop1ByCreationDateBeforeOrderByCreationDateDesc
                    (lastCreationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else {
            Object[] lastCities = weatherSettings.getViewedCities().toArray();
            if (lastCities.length > 0) {
                weatherCity = (WeatherCity) lastCities[0];
            } else {
                return "Нет ранее просмотренных элементов";
            }
        }

        if (weatherCity == null) {
            return "Больше нет отслеживаемых городов";
        }

        float longitude = weatherCity.getLongitude();
        float latitude = weatherCity.getLatitude();
        String city = weatherCity.getName();

        result.append(saveWeatherInfoAndDoMessageToUser(longitude,
                latitude, telegramUser, false, city));

        weatherSettings.setLastCityCreationDate(Date.from(weatherCity.getCreationDate().atZone(ZoneId.systemDefault()).toInstant()));
        Set<WeatherCity> viewedCities = weatherSettings.getViewedCities();
        if (viewedCities == null) {
            viewedCities = new HashSet<>();
        }

        if (isNextForecast) {
            WeatherCity lastViewedWeatherCity = weatherSettings.getLastViewedWeatherCity();
            if (lastViewedWeatherCity != null) {
                viewedCities.add(lastViewedWeatherCity);
            }
        } else {
            Object[] lastCities = viewedCities.toArray();
            if (lastCities.length > 1) {
                weatherSettings.setLastViewedWeatherCity((WeatherCity) lastCities[1]);
            }
            viewedCities.clear();
            for (int index = 1; index <= lastCities.length - 1; index++) {
                viewedCities.add((WeatherCity) lastCities[index]);
            }
        }

        weatherSettings.setLastViewedWeatherCity(weatherCity);
        weatherCityRepository.save(weatherCity);
        weatherSettingsRepository.save(weatherSettings);

        return result.toString();
    }

    private String saveWeatherInfoAndDoMessageToUser(float longitude, float latitude,
                                                     TelegramUser user, boolean isUserLocation, String city) {
        String messageToUser;

        YandexWeather weather = yandexWeatherService.getWeatherByCoordinates(Float.toString(longitude),
                Float.toString(latitude));

        if (weather == null) {
            messageToUser = "По вашему месторасположению не найдено информации о погоде!";
        } else {
            if (isUserLocation) {
                saveUserTZ(user, weather);
            }

            YandexWeatherFact fact = weather.getFact();

            messageToUser = "Погода в городе: " + "*" + city + "*" + "\r\n\r\n" +
                    "Сейчас " + yandexWeatherService.englishWeatherConditionToRussian(fact.getWeatherCondition()).toLowerCase()
                    + "\r\n" +
                    "Температура воздуха: " + fact.getTemp() + " ℃, по ощущениям: " + fact.getFeelsLike() + " ℃" + "\r\n" +
                    "Влажность: " + fact.getHumidity() + "%";
        }


        return messageToUser;
    }

    private String getNewsInfo(TelegramUser user, boolean isNextNews) {
        NewsSettings newsSettings = newsSettingsRepository.findByUserId(user.getId());
        StringBuilder result = new StringBuilder();
        List<NewsCategory> categoryList = new ArrayList<>();
        boolean isFollowingNews;

        if (newsSettings == null) {
            isFollowingNews = false;
        } else {
            categoryList = new ArrayList<>(newsSettings.getNewsCategories());
            isFollowingNews = newsSettings.isActiveUserSettings();
        }

        if (categoryList.isEmpty() && isFollowingNews) {
            result.append("Список отслеживаемых тем пуст");
        } else {
            if (getNewsAndMakeMessageFollowingNews(isNextNews, newsSettings, result, categoryList, isFollowingNews)) {
                return "Нет ранее просмотренных элементов";
            }
        }
        return result.toString();
    }

    private boolean getNewsAndMakeMessageFollowingNews(boolean isNextNews, NewsSettings newsSettings,
                                                       StringBuilder result, List<NewsCategory> categoryList,
                                                       boolean isFollowingNews) {
        NewsItem newsItem;
        if (isNextNews) {
            newsItem = getNextNewsItem(newsSettings, categoryList, isFollowingNews);
        } else {
            Object[] lastItems = newsSettings.getViewedNews().toArray();
            if (lastItems.length > 0) {
                newsItem = (NewsItem) lastItems[0];
            } else {
                return true;
            }
        }

        makeNewsMessageAndCountViews(result, newsItem, newsSettings, isNextNews);
        return false;
    }

    private NewsItem getNextNewsItem(NewsSettings newsSettings, List<NewsCategory> categoryList, boolean isFollowingNews) {
        NewsItem newsItem;
        Date lastPublicationDate = newsSettings.getLastNewsPublicationDate();
        if (lastPublicationDate == null) {
            lastPublicationDate = new Date();
        }

        if (isFollowingNews) {
            newsItem = newsItemRepository.findTop1ByCategoryListInAndPublicationDateBeforeOrderByPublicationDateDescCreationDateDesc(
                    categoryList, lastPublicationDate);
        } else {
            newsItem = newsItemRepository.findTop1ByIdIsNotNullAndPublicationDateBeforeOrderByPublicationDateDescCreationDateDesc(
                    lastPublicationDate);
        }
        return newsItem;
    }

    @Transactional
    void makeNewsMessageAndCountViews(StringBuilder result, NewsItem newsItem, NewsSettings newsSettings, boolean isNextNews) {
        result.append(makeNewsMessage(newsItem));

        newsItem.setCountOfViewers(newsItem.getCountOfViewers() + 1);

        newsSettings.setLastNewsPublicationDate(newsItem.getPublicationDate());
        Set<NewsItem> viewedItems = newsSettings.getViewedNews();
        if (viewedItems == null) {
            viewedItems = new HashSet<>();
        }

        if (isNextNews) {
            NewsItem lastViewedNewsItem = newsSettings.getLastViewedNewsItem();
            if (lastViewedNewsItem != null) {
                viewedItems.add(lastViewedNewsItem);
            }
        } else {
            Object[] lastItems = viewedItems.toArray();
            if (lastItems.length > 1) {
                newsSettings.setLastViewedNewsItem((NewsItem) lastItems[1]);
            }
            viewedItems.clear();
            for (int index = 1; index <= lastItems.length - 1; index++) {
                viewedItems.add((NewsItem) lastItems[index]);
            }
            // Не работает
//            viewedItems.remove(itemToDelete);
        }

        newsSettings.setLastViewedNewsItem(newsItem);
        newsItemRepository.save(newsItem);
        newsSettingsRepository.save(newsSettings);
    }

    private String makeNewsMessage(NewsItem newsItem) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = format.format(newsItem.getPublicationDate());
        String author = newsItem.getAuthor();
        List<NewsCategory> categoryList = newsItem.getCategoryList();
        StringBuilder categoriesSB = new StringBuilder();

        categoryList.forEach(category -> categoriesSB.append(category.getName()).append(", "));
        String categories = categoriesSB.toString();
        if (!categoryList.isEmpty()) {
            categories = categories.substring(0, categories.length() - 2);
        }

        return "*" + formattedDate + ". " +
                newsItem.getSource().getName() + " / " + categories +
                "\r\n\r\n" +
                ((author == null) ? "*" : "Автор: " + author + "*\r\n") +
                newsItem.getTitle() +
                "\r\n\r\n" +
                newsItem.getLink();
    }

    private String getTweetsInfo(TelegramUser user, boolean isNextTweets) {
        boolean isFollowingTweets;
        TwitterSettings twitterSettings = twitterSettingsRepository.findByUserId(user.getId());
        StringBuilder result = new StringBuilder();

        List<TwitterHashtag> hashtags = new ArrayList<>();
        List<TwitterPeople> nicknames = new ArrayList<>();

        if (twitterSettings == null) {
            isFollowingTweets = false;
        } else {
            hashtags = new ArrayList<>(twitterSettings.getTwitterHashtags());
            nicknames = new ArrayList<>(twitterSettings.getTwitterPeople());

            if (hashtags.size() == 0 && nicknames.size() == 0) {
                return "Не заполнены настройки Twitter";
            }
            isFollowingTweets = twitterSettings.isActiveUserSettings();
        }

        Tweet tweet;
        if (isNextTweets) {
            tweet = getNextTweet(isFollowingTweets, twitterSettings, hashtags, nicknames);
        } else {
            Object[] lastTweets = twitterSettings.getViewedTweets().toArray();
            if (lastTweets.length > 0) {
                tweet = (Tweet) lastTweets[0];
            } else {
                return "Нет ранее просмотренных элементов";
            }
        }

        makeTwitterMessageAndUpdateViewHistory(isNextTweets, twitterSettings, result, tweet);

        return result.toString();
    }

    private Tweet getNextTweet(boolean isFollowingTweets, TwitterSettings twitterSettings, List<TwitterHashtag> hashtags, List<TwitterPeople> nicknames) {
        Tweet tweet;
        Date lastCreatedAt = twitterSettings.getLastTweetCreationDate();
        if (lastCreatedAt == null) {
            lastCreatedAt = new Date();
        }

        if (isFollowingTweets) {
            tweet = tweetRepository.findTop1ByCreatedAtBeforeAndNicknameInOrHashtagInOrderByCreatedAtDesc(lastCreatedAt,
                    nicknames, hashtags);
        } else {
            tweet = tweetRepository.findTop1ByCreatedAtBeforeOrderByRetweetCountDescFavoriteCountDescCreatedAtDesc(lastCreatedAt);
        }
        return tweet;
    }

    private void makeTwitterMessageAndUpdateViewHistory(boolean isNextTweets, TwitterSettings twitterSettings, StringBuilder result, Tweet tweet) {
        result.append(makeTwitterMessage(tweet));
        twitterSettings.setLastTweetCreationDate(tweet.getCreatedAt());
        Set<Tweet> viewedTweets = twitterSettings.getViewedTweets();
        if (viewedTweets == null) {
            viewedTweets = new HashSet<>();
        }

        if (isNextTweets) {
            Tweet lastViewedTweet = twitterSettings.getLastViewedTweet();
            if (lastViewedTweet != null) {
                viewedTweets.add(lastViewedTweet);
            }
        } else {
            Object[] lastTweets = viewedTweets.toArray();
            if (lastTweets.length > 1) {
                twitterSettings.setLastViewedTweet((Tweet) lastTweets[1]);
            }
            viewedTweets.clear();
            for (int index = 1; index <= lastTweets.length - 1; index++) {
                viewedTweets.add((Tweet) lastTweets[index]);
            }
        }

        twitterSettings.setLastViewedTweet(tweet);
        tweetRepository.save(tweet);
        twitterSettingsRepository.save(twitterSettings);
    }

    private String makeTwitterMessage(Tweet tweet) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String formattedDate = format.format(tweet.getCreatedAt());
        String author = "@" + tweet.getFromUser();
        TwitterHashtag twitterHashtag = tweet.getHashtag();
        String hashtag = (twitterHashtag == null) ? null : "#" + twitterHashtag.getHashtag();

        return "*" + formattedDate + ". " + author + "*" + "\r\n\r\n"
                + tweet.getText() + "\r\n\r\n"
                + "Ретвитов: " + tweet.getRetweetCount().toString() + ". "
                + "Отметок «Нравится»: " + tweet.getFavoriteCount().toString() + "\r\n"
                + ((hashtag == null) ? "" : hashtag);
    }

    @Transactional
    void saveUserTZ(TelegramUser user, YandexWeather weather) {
        YandexWeatherTZInfo tzInfo = weather.getInfo().getTzInfo();
        user.setTzInfo(tzInfo);

        TelegramLocation userLocation = user.getLocation();
        userLocation.setTzInfo(tzInfo);

        userRepository.save(user);
        telegramLocationRepository.save(userLocation);
    }
    //endregion

}
