package pl.edu.agh.hiputs.visualization.connection.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfiguration {

  public static final String CARS_TOPIC = "cars";
  public static final String SIMULATION_STATE_CHANGE_TOPIC = "simulation_state_change";
  public static final String VISUALIZATION_STATE_CHANGE_TOPIC = "visualization_state_change";

  @Bean
  public NewTopic carsTopic() {
    return TopicBuilder.name(CARS_TOPIC).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic simulationStateChangeTopic() {
    return TopicBuilder.name(SIMULATION_STATE_CHANGE_TOPIC).partitions(1).replicas(1).build();
  }

  @Bean
  public NewTopic visualizationStateChangeTopic() {
    return TopicBuilder.name(VISUALIZATION_STATE_CHANGE_TOPIC).partitions(1).replicas(1).build();
  }
}
