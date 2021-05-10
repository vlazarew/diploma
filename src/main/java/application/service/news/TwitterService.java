package application.service.news;

import application.data.model.twitter.TwitterHashtag;
import application.data.model.twitter.TwitterPeople;
import application.data.model.twitter.Tweet;
import application.data.repository.twitter.TwitterHashtagRepository;
import application.data.repository.twitter.TwitterPeopleRepository;
import application.data.repository.twitter.TweetRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:application.properties")
@Log4j2
@EnableScheduling
@EnableAsync
public class TwitterService {

    @Value("${spring.social.twitter.appId}")
    String twitterAppId;

    @Value("${spring.social.twitter.appSecret}")
    String twitterAppSecret;

    Twitter twitter;

    @Autowired
    ModelMapper mapper;
    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    TwitterHashtagRepository twitterHashtagRepository;
    @Autowired
    TwitterPeopleRepository twitterPeopleRepository;

    @Scheduled(fixedRate = 300000)
    @Async
    public void updateTweets() {
        twitter = new TwitterTemplate(twitterAppId, twitterAppSecret);

        updateTweetsByHashtag(twitter);
        updateTweetsByPeople(twitter);
    }

    @Async
    void updateTweetsByHashtag(Twitter twitter) {
        Iterable<TwitterHashtag> followingHashtags = twitterHashtagRepository.findAll();

        followingHashtags.forEach(followingHashtag -> {
            String hashtag = followingHashtag.getHashtag();
            String searchedWord = hashtag.contains("#") ? hashtag : "#" + hashtag;

            List<org.springframework.social.twitter.api.Tweet> tweets = twitter.searchOperations().search(searchedWord)
                    .getTweets();

            tweets.forEach(tweet -> {
                Optional<Tweet> tweetInDBOptional = tweetRepository.findById(tweet.getId());
                Tweet customTweet;
                if (tweetInDBOptional.isPresent()) {
                    customTweet = tweetInDBOptional.get();
                    customTweet.setFavoriteCount(tweet.getFavoriteCount());
                    customTweet.setRetweetCount(tweet.getRetweetCount());
                    customTweet.setText(tweet.getText());
                } else {
                    customTweet = mapper.map(tweet, Tweet.class);
                    TwitterHashtag twitterHashtag = twitterHashtagRepository.findByHashtag(hashtag);
                    if (twitterHashtag == null) {
                        twitterHashtag = new TwitterHashtag();
                        twitterHashtag.setHashtag(hashtag);
                        twitterHashtagRepository.save(twitterHashtag);
                    }
                    customTweet.setHashtag(twitterHashtag);
                }

                tweetRepository.save(customTweet);
            });

        });
    }

    @Async
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void updateTweetsByPeople(Twitter twitter) {
        Iterable<TwitterPeople> followingPeoples = twitterPeopleRepository.findAll();

        followingPeoples.forEach(followingPeople -> {
            String nickname = followingPeople.getNickname();
            String searchedWord = nickname.contains("@") ? nickname.replace("@", "") : nickname;

            List<org.springframework.social.twitter.api.Tweet> tweets = twitter.searchOperations()
                    .search("from:" + searchedWord).getTweets();

            tweets.forEach(tweet -> {
                Optional<Tweet> tweetInDBOptional = tweetRepository.findById(tweet.getId());
                Tweet customTweet;
                if (tweetInDBOptional.isPresent()) {
                    customTweet = tweetInDBOptional.get();
                    customTweet.setFavoriteCount(tweet.getFavoriteCount());
                    customTweet.setRetweetCount(tweet.getRetweetCount());
                    customTweet.setText(tweet.getText());
                } else {
                    customTweet = mapper.map(tweet, Tweet.class);

                    TwitterPeople twitterPeople = twitterPeopleRepository.findByNickname(nickname);
                    if (twitterPeople == null) {
                        twitterPeople = new TwitterPeople();
                        twitterPeople.setNickname(nickname);
                        twitterPeopleRepository.save(twitterPeople);
                    }
                    customTweet.setNickname(twitterPeople);
                }

                tweetRepository.save(customTweet);
            });
        });
    }


}
