package pl.edu.agh.hiputs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.hiputs.model.follow.IdmDecider;
import pl.edu.agh.hiputs.model.follow.IDecider;

@Configuration
public class AppConfiguration {

    @Bean
    public IDecider decider() {
        return new IdmDecider();
    }

}
