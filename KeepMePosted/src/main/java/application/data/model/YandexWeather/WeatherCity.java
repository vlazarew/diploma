package application.data.model.YandexWeather;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class WeatherCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDateTime creationDate;

    String name;

    float longitude;
    float latitude;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }

}
