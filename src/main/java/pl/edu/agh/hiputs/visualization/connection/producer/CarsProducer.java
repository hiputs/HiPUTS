package pl.edu.agh.hiputs.visualization.connection.producer;

import static pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration.CARS_TOPIC;

import com.google.common.collect.Iterators;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;
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

  private boolean checkIsLaneIntersectingRegion(LaneReadable laneReadable, MapFragment mapFragment,
      ROIRegion roiRegion) {
    return checkIsJunctionInsideRegion(laneReadable.getIncomingJunctionId(), mapFragment, roiRegion)
        || checkIsJunctionInsideRegion(laneReadable.getOutgoingJunctionId(), mapFragment, roiRegion);
  }

  private boolean checkIsJunctionInsideRegion(JunctionId junctionId, MapFragment mapFragment,
      ROIRegion roiRegion) {
    return Optional.ofNullable(mapFragment.getJunctionReadable(junctionId)).stream()
        .allMatch(junctionReadable -> CoordinatesUtil.isCoordinatesInsideRegion(
            junctionReadable.getLongitude(),
            junctionReadable.getLatitude(),
            roiRegion.getTopLeftCoordinates().getLongitude(),
            roiRegion.getTopLeftCoordinates().getLatitude(),
            roiRegion.getBottomRightCoordinates().getLongitude(),
            roiRegion.getBottomRightCoordinates().getLatitude()));
  }

  private Stream<CarMessage> createCarsMessagesStream(MapFragment mapFragment, ROIRegion roiRegion) {
    return mapFragment.getKnownPatchReadable()
        .stream()
        .filter(patchReader -> mapFragment.isLocalPatch(patchReader.getPatchId()))
        .flatMap(PatchReader::streamLanesReadable)
        .filter(laneReadable -> CoordinatesUtils.isRegionEmpty(roiRegion) || checkIsLaneIntersectingRegion(laneReadable,
            mapFragment, roiRegion))
        .flatMap(LaneReadable::streamCarsFromExitReadable)
        .map(carReadable -> createCarMessage(carReadable, mapFragment));
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

  public void sendCars(MapFragment mapFragment, int iterationNumber,
      VisualizationStateChangeMessage visualizationStateChangeMessage) {
    log.info("[{}] Start sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId().getId());
    ROIRegion roiRegion = visualizationStateChangeMessage.getRoiRegion();

    Stream<CarMessage> carsMessagesStream = createCarsMessagesStream(mapFragment, roiRegion);
    Iterators.partition(carsMessagesStream.iterator(), 1000)
        .forEachRemaining(carMessageList -> sendCarMessageList(carMessageList, mapFragment, iterationNumber));

    log.info("[{}] End sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId().getId());
  }

  private void sendCarMessageList(List<CarMessage> carMessageList, MapFragment mapFragment, int iterationNumber) {
    CarsMessage carsMessage = CarsMessage.newBuilder()
        .setIterationNumber(iterationNumber)
        .addAllCarsMessages(carMessageList)
        .build();

    var record = new ProducerRecord<>(CARS_TOPIC, mapFragment.getMapFragmentId().getId(), carsMessage);

    try {
      this.kafkaCarTemplate.send(record).addCallback(new ListenableFutureCallback<>() {
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
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
