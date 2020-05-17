package application.service.news;

import application.data.model.news.NewsCategory;
import application.data.model.news.NewsItem;
import application.data.model.news.NewsSource;
import application.data.repository.news.NewsCategoryRepository;
import application.data.repository.news.NewsItemRepository;
import application.data.repository.news.NewsSourceRepository;
import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@PropertySource("classpath:news.properties")
@Log4j2
@EnableScheduling
@EnableAsync
public class NewsService {

    //region rss-links
    @Value("${news.rbk.rss}")
    String rbkRSSLink;

    @Value("${news.riatass.rss}")
    String riaTassRSSLink;

    @Value("${news.vestiru.rss}")
    String vestiRuRSSLink;

    @Value("${news.vedomosti.rss}")
    String vedomostiRSSLink;

    @Value("${news.izvestiya.rss}")
    String izvestiyaRSSLink;

    @Value("${news.lentaru.rss}")
    String lentaRuRSSLink;
    //endregion

    final HashMap<String, String> rbkCategories = new HashMap<String, String>() {{
        put("rbcfreenews", "Без категории");
        put("economics", "Экономика");
        put("business", "Бизнес");
        put("opinions", "Мнения");
        put("finances", "Финансы");
        put("technology_and_media", "Технологии и медиа");
        put("politics", "Политика");
        put("society", "Общество");
        put("photoreport", "Фотоотчет");
        put("money", "Деньги");
        put("own_business", "Наш бизнес");
    }};

    @Autowired
    NewsSourceRepository newsSourceRepository;

    @Autowired
    NewsItemRepository newsItemRepository;

    @Autowired
    NewsCategoryRepository newsCategoryRepository;

    @Scheduled(fixedRate = 60000)
    @Async
    public void updateNews() throws IOException, FeedException {
        updateNewsSource(rbkRSSLink);
        updateNewsSource(riaTassRSSLink);
        updateNewsSource(vestiRuRSSLink);
        updateNewsSource(vedomostiRSSLink);
        updateNewsSource(izvestiyaRSSLink);
        updateNewsSource(lentaRuRSSLink);
    }

    @Async
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void updateNewsSource(String rssLink) throws IOException, FeedException {
        SyndFeed feed = getSyndFeed(rssLink);
        boolean categoryFromHashMap = (rssLink.equals(rbkRSSLink) || rssLink.equals(izvestiyaRSSLink));

        String sourceName = feed.getDescription();
        NewsSource newsSource = findCreateNewsSource(feed, sourceName);

        List<SyndEntryImpl> entries = feed.getEntries();
        entries.forEach(entry -> {
            String uri = entry.getUri();
            NewsItem newsItem = newsItemRepository.findByUri(uri);

            if (newsItem == null) {
                createNewsItem(newsSource, entry, uri, categoryFromHashMap);
            }
        });
    }

    private SyndFeed getSyndFeed(String rssLink) throws IOException, FeedException {
        URL feedSource = new URL(rssLink);
        SyndFeedInput input = new SyndFeedInput();
        XmlReader reader = new XmlReader(feedSource);
        return input.build(reader);
    }

    private void createNewsItem(NewsSource newsSource, SyndEntryImpl entry, String uri, boolean categoryFromHashMap) {
        String link = entry.getLink();

        NewsItem newsItem = new NewsItem();
        newsItem.setUri(uri);
        newsItem.setLink(link);
        newsItem.setSource(newsSource);
        newsItem.setTitle(entry.getTitle());

        setDescription(entry, newsItem);
        setAuthorPublicationDate(entry, newsItem);
        setPhotoUrl(entry, newsItem);

        if (categoryFromHashMap) {
            setNewsCategoriesFromHashMap(link, newsItem);
        } else {
            setNewsCategories(entry);
        }

        newsItemRepository.save(newsItem);
    }

    private void setNewsCategories(SyndEntryImpl entry) {
        ArrayList<NewsCategory> newsCategories = new ArrayList<>();

        List<SyndCategoryImpl> categoryList = entry.getCategories();
        categoryList.forEach(category -> {
            String categoryName = category.getName();
            NewsCategory newsCategory = findCreateCategory(categoryName);

            newsCategories.add(newsCategory);
        });
    }

    private void setNewsCategoriesFromHashMap(String link, NewsItem newsItem) {
        ArrayList<NewsCategory> newsCategories = new ArrayList<>();

        rbkCategories.forEach((s, s2) -> {
            if (link.contains(s)) {
                newsCategories.add(findCreateCategory(s2));
            }
        });

        if (newsCategories.isEmpty()) {
            newsCategories.add(findCreateCategory("Без категории"));
        }

        newsItem.setCategoryList(newsCategories);
    }

    private NewsCategory findCreateCategory(String categoryName) {
        NewsCategory newsCategory = newsCategoryRepository.findByName(categoryName);
        if (newsCategory == null) {
            newsCategory = new NewsCategory();
            newsCategory.setName(categoryName);

            newsCategoryRepository.save(newsCategory);
        }
        return newsCategory;
    }

    private void setPhotoUrl(SyndEntryImpl entry, NewsItem newsItem) {
        List<SyndEnclosureImpl> enclosureList = entry.getEnclosures();
        if (enclosureList.size() > 0) {
            newsItem.setPhotoUrl(enclosureList.get(0).getUrl());
        }
    }

    private void setDescription(SyndEntryImpl entry, NewsItem newsItem) {
        SyndContent description = entry.getDescription();
        if (description != null) {
            newsItem.setDescription(description.getValue());
        }
    }

    private void setAuthorPublicationDate(SyndEntryImpl entry, NewsItem newsItem) {
        List<DCModuleImpl> dcModuleList = entry.getModules();
        if (dcModuleList.size() > 0) {
            DCModuleImpl firstDCModule = dcModuleList.get(0);

            newsItem.setAuthor(firstDCModule.getCreator());
            newsItem.setPublicationDate(firstDCModule.getDate());
        }
    }

    private NewsSource findCreateNewsSource(SyndFeed feed, String sourceName) {
        return newsSourceRepository.findByName(sourceName).orElseGet(() -> {
            NewsSource newNewsSource = new NewsSource();
            newNewsSource.setName(sourceName);
            newNewsSource.setLastUpdate(LocalDateTime.now());
            newNewsSource.setLink(feed.getLink());

            SyndImage image = feed.getImage();
            if (image != null) {
                newNewsSource.setLogoImageUrl(image.getUrl());
            }

            return newsSourceRepository.save(newNewsSource);
        });
    }

}
