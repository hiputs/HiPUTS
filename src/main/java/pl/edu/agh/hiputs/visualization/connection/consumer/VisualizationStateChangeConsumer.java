package pl.edu.agh.hiputs.visualization.connection.consumer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.VISUALIZATION_STATE_CHANGE_TOPIC;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.visualization.connection.VisualizationSynchronisationService;
import proto.model.VisualizationStateChangeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationStateChangeConsumer {

  private final VisualizationSynchronisationService visualizationSynchronisationService;

  @Getter
  private VisualizationStateChangeMessage currentVisualizationStateChangeMessage;

  @KafkaListener(
      topics = VISUALIZATION_STATE_CHANGE_TOPIC,
      groupId = VISUALIZATION_STATE_CHANGE_TOPIC,
      id = VISUALIZATION_STATE_CHANGE_TOPIC,
      autoStartup = "false",
      properties = {"specific.protobuf.value.type: proto.model.VisualizationStateChangeMessage"})
  void stateChangeListener(ConsumerRecord<String, VisualizationStateChangeMessage> record) {
    currentVisualizationStateChangeMessage = record.value();
    log.info("Consumed VisualizationStateChangeMessage: stateChange={}, ROIRegion={}, ZoomLevel={}, VisualizationSpeed={}",
        currentVisualizationStateChangeMessage.getStateChange(), currentVisualizationStateChangeMessage.getRoiRegion(),
        currentVisualizationStateChangeMessage.getZoomLevel(), currentVisualizationStateChangeMessage.getVisualizationSpeed());
    visualizationSynchronisationService.applyVisualizationChange(currentVisualizationStateChangeMessage);
  }
}