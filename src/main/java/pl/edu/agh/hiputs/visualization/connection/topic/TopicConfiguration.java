package pl.edu.agh.hiputs.visualization.connection.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfiguration {

  public static final String CARS_TOPIC = "cars";

  @Bean
  public NewTopic carsTopic() {
    return TopicBuilder.name(CARS_TOPIC).partitions(1).replicas(1).build();
  }
}
