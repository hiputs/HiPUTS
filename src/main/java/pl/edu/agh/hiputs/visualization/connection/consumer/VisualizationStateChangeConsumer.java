package pl.edu.agh.hiputs.visualization.connection.consumer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.VISUALIZATION_STATE_CHANGE_TOPIC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.visualization.events.ApplicationClosedEvent;
import pl.edu.agh.hiputs.visualization.events.ApplicationResumedEvent;
import pl.edu.agh.hiputs.visualization.events.ApplicationStartedEvent;
import pl.edu.agh.hiputs.visualization.events.ApplicationStoppedEvent;
import proto.model.VisualizationStateChangeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationStateChangeConsumer {

  private final ApplicationContext applicationContext;

  @KafkaListener(topics = VISUALIZATION_STATE_CHANGE_TOPIC, groupId = VISUALIZATION_STATE_CHANGE_TOPIC,
      properties = {"specific.protobuf.value.type: proto.model.VisualizationStateChangeMessage"})
  void stateChangeListener(ConsumerRecord<String, VisualizationStateChangeMessage> record) {
    VisualizationStateChangeMessage visualizationStateChangeMessage = record.value();
    log.info("Consumed VisualizationStateChangeMessage:{}", visualizationStateChangeMessage.getStateChange());

    switch (visualizationStateChangeMessage.getStateChange()) {
      case STARTED -> applicationContext.publishEvent(new ApplicationStartedEvent(this));
      case RESUMED -> applicationContext.publishEvent(new ApplicationResumedEvent(this));
      case STOPPED -> applicationContext.publishEvent(new ApplicationStoppedEvent(this));
      case CLOSED -> applicationContext.publishEvent(new ApplicationClosedEvent(this));
    }
  }
}