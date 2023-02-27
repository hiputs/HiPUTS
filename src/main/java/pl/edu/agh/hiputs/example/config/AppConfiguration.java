package pl.edu.agh.hiputs.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IdmDecider;

@Configuration
public class AppConfiguration {

  @Bean
  public FunctionalDecider decider() {
    return new IdmDecider();
  }

}
