package application.data.model.telegram;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@Data
@Setter
public abstract class AbstractTelegramEntity implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-HH hh:mm:ss")
    LocalDateTime creationDate;

    @PrePersist
    public void toCreate() {
        setCreationDate(LocalDateTime.now());
    }
}
