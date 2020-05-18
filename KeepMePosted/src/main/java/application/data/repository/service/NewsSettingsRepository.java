package application.data.repository.service;

import application.data.model.service.NewsSettings;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsSettingsRepository extends CrudRepository<NewsSettings, Long> {

    List<NewsSettings> findByUserId(Integer userId);

    NewsSettings findByUserIdAndKeyword(Integer userId, String keyword);

//    @Query(value = "select NewsSettings.keyword from NewsSettings where NewsSettings.user.id = :userId")
//    @Lazy
//    List<String> getKeywordsByUserId(@Param("userId") Integer userId);

}
