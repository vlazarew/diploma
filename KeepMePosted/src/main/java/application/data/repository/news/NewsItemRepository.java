package application.data.repository.news;

import application.data.model.news.NewsItem;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NewsItemRepository extends CrudRepository<NewsItem, Long> {

    NewsItem findByUri(String uri);

}
