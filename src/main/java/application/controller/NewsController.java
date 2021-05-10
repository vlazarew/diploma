package application.controller;

import application.data.model.news.NewsItem;
import application.data.repository.news.NewsItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping(value = "/news", produces = "application/json")
@CrossOrigin("*")
public class NewsController {

    @Autowired
    NewsItemRepository newsItemRepository;

    @GetMapping(params = {"numberOfPage", "newsOnPage", "typeOfTimePeriod"}, produces = "application/json")
    public ArrayList<Object> allNews(@RequestParam("numberOfPage") int numberOfPage,
                                     @RequestParam("newsOnPage") int newsOnPage,
                                     @RequestParam("typeOfTimePeriod") String typeOfTimePeriod) {
        PageRequest page = PageRequest.of(numberOfPage, newsOnPage, Sort.by("countOfViewers")
                .and(Sort.by("publicationDate").descending())
                .and(Sort.by("creationDate").descending()));

        LocalDateTime ldt = getLocalDateTime(typeOfTimePeriod);
        Date outStart = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        Date outEnd = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

        ArrayList result = new ArrayList<>();
        Page<NewsItem> queryResult = newsItemRepository.findByPublicationDateBetweenOrderByCountOfViewersDescPublicationDateDescCreationDateDesc(outStart,
                outEnd, page);
        result.add(queryResult.getContent());
        result.add(queryResult.getTotalElements());
        result.add(queryResult.getTotalPages());

        return result;
    }

    @PostMapping(value = "/add_count_of_views", consumes = "application/json")
    @ResponseStatus(HttpStatus.CHECKPOINT)
    public void updateCountOfViews(@RequestBody long id) {
        Optional<NewsItem> newsItemOptional = newsItemRepository.findById(id);
        if (newsItemOptional.isPresent()) {
            NewsItem newsItem = newsItemOptional.get();
            newsItem.setCountOfViewers(newsItem.getCountOfViewers() + 1);
            newsItemRepository.save(newsItem);
        }
    }

    private LocalDateTime getLocalDateTime(@RequestParam("typeOfTimePeriod") String typeOfTimePeriod) {
        LocalDateTime ldt = LocalDateTime.now();
        switch (typeOfTimePeriod) {
            case "10min": {
                ldt = ldt.minusMinutes(10);
                break;
            }
            case "1h": {
                ldt = ldt.minusHours(1);
                break;
            }
            case "1d": {
                ldt = ldt.minusDays(1);
                break;
            }
            case "1w": {
                ldt = ldt.minusWeeks(1);
                break;
            }
            case "1m": {
                ldt = ldt.minusMonths(1);
                break;
            }
            case "1y": {
                ldt = ldt.minusYears(1);
                break;
            }

        }
        return ldt;
    }

}
