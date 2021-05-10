package application.data.repository.news;

import application.data.model.news.NewsSource;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NewsSourceRepository extends CrudRepository<NewsSource, Long> {

    Optional<NewsSource> findBySourceName(String sourceName);

    NewsSource findByName(String name);
}
