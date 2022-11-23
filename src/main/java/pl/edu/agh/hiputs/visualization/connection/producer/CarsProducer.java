package pl.edu.agh.hiputs.visualization.connection.producer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.CARS_TOPIC;

import java.util.List;
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
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;
import pl.edu.agh.hiputs.visualization.connection.consumer.VisualizationStateChangeConsumer;
import proto.model.CarMessage;
import proto.model.CarsMessage;
import proto.model.VisualizationStateChangeMessage.ROIRegion;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsProducer {

  private final KafkaTemplate<String, CarsMessage> kafkaCarTemplate;
  private final VisualizationStateChangeConsumer visualizationStateChangeConsumer;

  private static boolean checkIsLaneIntersectingRegion(LaneReadable laneReadable, MapFragment mapFragment, ROIRegion roiRegion) {
    return checkIsJunctionInsideRegion(laneReadable.getIncomingJunctionId(), mapFragment, roiRegion)
        || checkIsJunctionInsideRegion(laneReadable.getOutgoingJunctionId(), mapFragment, roiRegion);
  }

  private static boolean checkIsJunctionInsideRegion(JunctionId junctionId, MapFragment mapFragment, ROIRegion roiRegion) {
    JunctionReadable junctionReadable = mapFragment.getJunctionReadable(junctionId);
    return CoordinatesUtil.isCoordinatesInsideRegion(
        junctionReadable.getLongitude(),
        junctionReadable.getLatitude(),
        roiRegion.getTopLeftCoordinates().getLongitude(),
        roiRegion.getTopLeftCoordinates().getLatitude(),
        roiRegion.getBottomRightCoordinates().getLongitude(),
        roiRegion.getBottomRightCoordinates().getLatitude()
    );
  }

  private static List<CarMessage> createCarsMessagesList(MapFragment mapFragment, ROIRegion roiRegion) {
    return mapFragment.getKnownPatchReadable().stream()
        .flatMap(PatchReader::streamLanesReadable)
        .filter(laneReadable -> checkIsLaneIntersectingRegion(laneReadable, mapFragment, roiRegion))
        .flatMap(LaneReadable::streamCarsFromExitReadable)
        .map(carReadable -> createCarMessage(carReadable, mapFragment))
        .toList();
  }

  private static CarMessage createCarMessage(CarReadable car, MapFragment mapFragment) {
    LaneReadable carLane = mapFragment.getLaneReadable(car.getLaneId());
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

    ROIRegion roiRegion = visualizationStateChangeConsumer.getCurrentVisualizationStateChangeMessage().getRoiRegion();
    CarsMessage carsMessage = CarsMessage.newBuilder()
        .setIterationNumber(iterationNumber)
        .addAllCarsMessages(createCarsMessagesList(mapFragment, roiRegion))
        .build();

    var record = new ProducerRecord<>(CARS_TOPIC, mapFragment.getMapFragmentId().toString(), carsMessage);
    ListenableFuture<SendResult<String, CarsMessage>> future = this.kafkaCarTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, CarsMessage> result) {
        log.info("[{}] Send {} cars from mapFragment: {}", iterationNumber, carsMessage.getCarsMessagesCount(),
            mapFragment.getMapFragmentId().getId());
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.error("[{}] Error while sending carsMessage: {}", iterationNumber, ex.getMessage());
      }
    });

    log.info("[{}] End sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId().getId());
  }
}
