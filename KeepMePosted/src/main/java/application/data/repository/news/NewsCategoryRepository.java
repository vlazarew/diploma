package application.data.repository.news;

import application.data.model.news.NewsCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NewsCategoryRepository extends CrudRepository<NewsCategory, Long> {

    NewsCategory findByName(String name);

}
