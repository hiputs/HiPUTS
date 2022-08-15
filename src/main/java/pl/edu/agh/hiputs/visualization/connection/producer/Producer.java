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
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration;
import proto.model.CarMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class Producer {

  private final KafkaTemplate<String, CarMessage> kafkaCarTemplate;

  public void sendCar(CarReadable car, LaneReadable lane, String patchId) {
    double positionOnLane = car.getPositionOnLane() / lane.getLength();

    CarMessage carMessage = CarMessage.newBuilder()
        .setCarId(car.getCarId().getValue())
        .setLength(car.getLength())
        .setAcceleration(car.getAcceleration())
        .setSpeed(car.getSpeed())
        .setMaxSpeed(car.getMaxSpeed())
        .setLaneId(car.getLaneId().getValue())
        .setPositionOnLane(positionOnLane)
        .build();

    var record = new ProducerRecord<String, CarMessage>(TopicConfiguration.CARS_TOPIC, patchId, carMessage);
    ListenableFuture<SendResult<String, CarMessage>> future = this.kafkaCarTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, CarMessage> result) {
        // log.info("Sent:" + car + " , offset:" + result.getRecordMetadata().offset());
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.error("Error while sending car:" + car + " " + ex.getMessage());
      }
    });
  }
}
