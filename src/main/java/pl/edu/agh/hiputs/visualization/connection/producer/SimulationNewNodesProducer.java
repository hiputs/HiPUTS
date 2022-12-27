package pl.edu.agh.hiputs.visualization.connection.producer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.SIMULATION_NEW_NODES_TOPIC;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import proto.model.Coordinates;
import proto.model.Node;
import proto.model.SimulationNewNodesTransferMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationNewNodesProducer {

  private final KafkaTemplate<String, SimulationNewNodesTransferMessage> kafkaTemplate;

  private Node createNodeFromJunction(JunctionReadable junctionReadable) {
    return Node.newBuilder()
        .setNodeId(junctionReadable.getJunctionId().getValue())
        .setCoordinates(Coordinates.newBuilder()
            .setLongitude(junctionReadable.getLongitude())
            .setLatitude(junctionReadable.getLatitude())
            .build())
        .build();
  }

  private List<Node> getNotOsmNodesList(List<Patch> patches) {
    return patches.stream()
        .flatMap(Patch::streamJunctionsReadable)
        .filter(junctionReadable -> !junctionReadable.getJunctionId().isOsmNode())
        .map(this::createNodeFromJunction)
        .collect(Collectors.toList());
  }

  public void sendSimulationNotOsmNodesTransferMessage(List<Patch> patches) {
    List<Node> notOsmNodesList = getNotOsmNodesList(patches);

    SimulationNewNodesTransferMessage simulationNotOsmNodesTransferMessage =
        SimulationNewNodesTransferMessage.newBuilder().addAllNodes(notOsmNodesList).build();

    var record = new ProducerRecord<String, SimulationNewNodesTransferMessage>(SIMULATION_NEW_NODES_TOPIC,
        simulationNotOsmNodesTransferMessage);

    ListenableFuture<SendResult<String, SimulationNewNodesTransferMessage>> future = kafkaTemplate.send(record);
    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, SimulationNewNodesTransferMessage> result) {
        log.info("SimulationNewNodesTransferMessage send {} new nodes",
            simulationNotOsmNodesTransferMessage.getNodesCount());
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.info("Error while sending message: {}", ex.getMessage());
      }
    });
  }

}
