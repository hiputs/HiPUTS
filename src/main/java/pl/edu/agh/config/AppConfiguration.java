package pl.edu.agh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.model.follow.IDMDecider;
import pl.edu.agh.model.follow.IDecider;

@Configuration
public class AppConfiguration {

    @Bean
    public IDecider decider() {
        return new IDMDecider();
    }

}
