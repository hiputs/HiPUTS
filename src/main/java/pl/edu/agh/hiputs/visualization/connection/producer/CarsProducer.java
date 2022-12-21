package pl.edu.agh.hiputs.visualization.connection.producer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.CARS_TOPIC;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;
import pl.edu.agh.hiputs.visualization.connection.consumer.VisualizationStateChangeConsumer;
import pl.edu.agh.hiputs.visualization.utils.CoordinatesUtils;
import proto.model.CarMessage;
import proto.model.CarsMessage;
import proto.model.VisualizationStateChangeMessage;
import proto.model.VisualizationStateChangeMessage.ROIRegion;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsProducer {

  private final KafkaTemplate<String, CarsMessage> kafkaCarTemplate;
  private final VisualizationStateChangeConsumer visualizationStateChangeConsumer;

  private static boolean checkIsLaneIntersectingRegion(LaneReadable laneReadable, PatchReader patchReader,
      ROIRegion roiRegion) {
    return checkIsJunctionInsideRegion(laneReadable.getIncomingJunctionId(), patchReader, roiRegion)
        || checkIsJunctionInsideRegion(laneReadable.getOutgoingJunctionId(), patchReader, roiRegion);
  }

  private static boolean checkIsJunctionInsideRegion(JunctionId junctionId, PatchReader patchReader,
      ROIRegion roiRegion) {
    return Optional.ofNullable(patchReader.getJunctionReadable(junctionId))
        .stream()
        .allMatch(junctionReadable -> CoordinatesUtil.isCoordinatesInsideRegion(junctionReadable.getLongitude(),
            junctionReadable.getLatitude(), roiRegion.getTopLeftCoordinates().getLongitude(),
            roiRegion.getTopLeftCoordinates().getLatitude(), roiRegion.getBottomRightCoordinates().getLongitude(),
            roiRegion.getBottomRightCoordinates().getLatitude()));
  }

  private static List<CarMessage> createCarsMessagesList(PatchReader patchReader, ROIRegion roiRegion) {
    return patchReader.streamLanesReadable()
        .filter(laneReadable -> CoordinatesUtils.isRegionEmpty(roiRegion) || checkIsLaneIntersectingRegion(laneReadable,
            patchReader, roiRegion))
        .flatMap(LaneReadable::streamCarsFromExitReadable)
        .map(carReadable -> createCarMessage(carReadable, patchReader))
        .toList();
  }

  private static CarMessage createCarMessage(CarReadable car, PatchReader patchReader) {
    LaneReadable carLane = patchReader.getLaneReadable(car.getLaneId());
    double positionOnLane = car.getPositionOnLane() / carLane.getLength();
    return CarMessage.newBuilder()
        .setCarId(car.getCarId().getValue())
        .setLength(car.getLength())
        .setAcceleration(car.getAcceleration())
        .setSpeed(car.getSpeed())
        .setMaxSpeed(car.getMaxSpeed())
        .setNode1Id(carLane.getIncomingJunctionId().getValue())
        .setNode2Id(carLane.getOutgoingJunctionId().getValue())
        .setPositionOnLane(positionOnLane)
        .build();
  }

  public void sendCars(MapFragment mapFragment, int iterationNumber) {
    log.info("[{}] Start sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId().getId());
    VisualizationStateChangeMessage visualizationStateChangeMessage =
        visualizationStateChangeConsumer.getCurrentVisualizationStateChangeMessage();
    if (null == visualizationStateChangeMessage) {
      log.info("[{}] Visualization not started yet", iterationNumber);
      return;
    }
    ROIRegion roiRegion = visualizationStateChangeMessage.getRoiRegion();

    mapFragment.getKnownPatchReadable().forEach(patchReader -> {
      List<CarMessage> carMessageList = createCarsMessagesList(patchReader, roiRegion);
      if (!carMessageList.isEmpty()) {
        sendCarMessageList(carMessageList, patchReader, iterationNumber);
      }
    });
    log.info("[{}] End sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId().getId());
  }

  public void sendCarMessageList(List<CarMessage> carMessageList, PatchReader patchReader, int iterationNumber) {
    CarsMessage carsMessage = CarsMessage.newBuilder()
        .setIterationNumber(iterationNumber)
        .addAllCarsMessages(carMessageList)
        .build();

    var record = new ProducerRecord<>(CARS_TOPIC, patchReader.getPatchId().getValue(), carsMessage);
    ListenableFuture<SendResult<String, CarsMessage>> future = this.kafkaCarTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, CarsMessage> result) {
        log.info("[{}] Send {} cars from patch: {}", iterationNumber, carsMessage.getCarsMessagesCount(),
            patchReader.getPatchId().getValue());
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.error("[{}] Error while sending carsMessage: {}", iterationNumber, ex.getMessage());
      }
    });
  }
}
