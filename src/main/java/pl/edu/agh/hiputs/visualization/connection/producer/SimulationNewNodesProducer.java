package pl.edu.agh.hiputs.visualization.connection.producer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.SIMULATION_NEW_NODES_TOPIC;

import com.google.common.collect.Iterators;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
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

  private Stream<Node> getNotOsmNodesList(MapFragment mapFragment) {
    return mapFragment.getKnownPatchReadable()
        .stream()
        .filter(patchReader -> mapFragment.isLocalPatch(patchReader.getPatchId()))
        .flatMap(PatchReader::streamJunctionsReadable)
        .filter(junctionReadable -> !junctionReadable.getJunctionId().isOsmNode())
        .map(this::createNodeFromJunction);
  }

  public void sendSimulationNotOsmNodesTransferMessage(MapFragment mapFragment) {
    Stream<Node> notOsmNodesStream = getNotOsmNodesList(mapFragment);
    Iterators.partition(notOsmNodesStream.iterator(), 1000).forEachRemaining(this::sendNewNodesList);
  }

  private void sendNewNodesList(List<Node> newNodesList) {
    SimulationNewNodesTransferMessage simulationNotOsmNodesTransferMessage =
        SimulationNewNodesTransferMessage.newBuilder().addAllNodes(newNodesList).build();

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
