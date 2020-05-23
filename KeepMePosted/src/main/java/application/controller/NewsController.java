package application.controller;

import application.data.model.news.NewsItem;
import application.data.repository.news.NewsItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping(value = "/news", produces = "application/json")
@CrossOrigin("*")
public class NewsController {

    @Autowired
    NewsItemRepository newsItemRepository;

    @GetMapping(params = {"numberOfPage", "newsOnPage", "typeOfTimePeriod"}, produces = "application/json")
    public Iterable<NewsItem> allNews(@RequestParam("numberOfPage") int numberOfPage,
                                      @RequestParam("newsOnPage") int newsOnPage,
                                      @RequestParam("typeOfTimePeriod") String typeOfTimePeriod) {
        PageRequest page = PageRequest.of(numberOfPage, newsOnPage, Sort.by("countOfViewers")
                .and(Sort.by("publicationDate").descending())
                .and(Sort.by("creationDate").descending()));

        LocalDateTime ldt = getLocalDateTime(typeOfTimePeriod);
        Date outStart = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        Date outEnd = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

        return newsItemRepository.findByPublicationDateBetweenOrderByCountOfViewersDescPublicationDateDescCreationDateDesc(outStart,
                outEnd, page).getContent();
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
