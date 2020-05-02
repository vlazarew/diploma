package application;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
@EnableScheduling
public class KeepMePostedApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(KeepMePostedApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        return mapper;
    }

}
