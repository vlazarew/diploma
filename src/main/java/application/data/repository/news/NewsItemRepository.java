package application.data.repository.news;

import application.data.model.news.NewsCategory;
import application.data.model.news.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    NewsItem findByUri(String uri);

//    @Query("select TOP 10 items FROM NewsItem items " +
//            "where " +
//            "order by items.publicationDate desc, items.creationDate desc ")
//    List<NewsItem> findTop15();
//
//    List<String> findDistinctFirstByAuthor(String author);

//    List<NewsItem> findTop15ByTitleContainingOrderByPublicationDateDesc(List<String> title);

    NewsItem findTop1ByCategoryListInAndPublicationDateBeforeOrderByPublicationDateDescCreationDateDesc(List<NewsCategory> categoryList,
                                                                                                     Date publicationDate);

    NewsItem findTop1ByIdIsNotNullAndPublicationDateBeforeOrderByPublicationDateDescCreationDateDesc(Date publicationDate);
//    List<NewsItem> findTop10ByIdIsNotNullOrderByPublicationDateDescCreationDateDesc();

    Page<NewsItem> findByPublicationDateBetweenOrderByCountOfViewersDescPublicationDateDescCreationDateDesc(Date publicationDateStart,
                                                                                                            Date publicationDateEnd,
                                                                                                            Pageable pageable);
}
