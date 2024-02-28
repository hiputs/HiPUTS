package pl.edu.agh.hiputs.visualization.connection.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration;
import proto.model.RUNNING_STATE;
import proto.model.SimulationStateChangeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationStateChangeProducer {

  private final KafkaTemplate<String, SimulationStateChangeMessage> kafkaSimulationStateTemplate;

  public void sendStateChangeMessage(RUNNING_STATE running_state) {
    SimulationStateChangeMessage simulationStateChangeMessage =
        SimulationStateChangeMessage.newBuilder().setStateChange(running_state).build();

    var record =
        new ProducerRecord<String, SimulationStateChangeMessage>(TopicConfiguration.SIMULATION_STATE_CHANGE_TOPIC,
            simulationStateChangeMessage);
    ListenableFuture<SendResult<String, SimulationStateChangeMessage>> future =
        kafkaSimulationStateTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, SimulationStateChangeMessage> result) {
        log.info("SimulationStateChangeMessage send: {}", simulationStateChangeMessage.getStateChange());
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.info("Error while sending message: {}", simulationStateChangeMessage + " " + ex.getMessage());
      }
    });
  }

}
