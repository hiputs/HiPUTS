package pl.edu.agh.hiputs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.hiputs.model.follow.IDecider;
import pl.edu.agh.hiputs.model.follow.IdmDecider;

@Configuration
public class AppConfiguration {

  @Bean
  public IDecider decider() {
    return new IdmDecider();
  }

}
