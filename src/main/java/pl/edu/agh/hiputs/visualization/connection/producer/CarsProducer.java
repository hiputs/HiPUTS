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
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import proto.model.CarMessage;
import proto.model.CarsMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsProducer {

  private final KafkaTemplate<String, CarsMessage> kafkaCarTemplate;

  private static List<CarMessage> createCarsMessagesList(MapFragment mapFragment) {
    return mapFragment.getKnownPatchReadable().stream()
        .flatMap(PatchReader::streamLanesReadable)
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
    log.info("[{}] Start sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId());

    CarsMessage carsMessage = CarsMessage.newBuilder()
        .setIterationNumber(iterationNumber)
        .addAllCarsMessages(createCarsMessagesList(mapFragment))
        .build();

    var record = new ProducerRecord<>(CARS_TOPIC, mapFragment.getMapFragmentId().toString(), carsMessage);
    ListenableFuture<SendResult<String, CarsMessage>> future = this.kafkaCarTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, CarsMessage> result) {
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.error("[{}] Error while sending carsMessage: {}", iterationNumber, ex.getMessage());
      }
    });

    log.info("[{}] End sending cars from map fragment: {}", iterationNumber, mapFragment.getMapFragmentId());
  }
}
