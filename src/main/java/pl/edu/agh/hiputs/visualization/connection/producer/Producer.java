package pl.edu.agh.hiputs.visualization.connection.producer;

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
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.visualization.connection.topic.TopicConfiguration;
import proto.model.CarMessage;
import proto.model.CarsMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class Producer {

  private final KafkaTemplate<String, CarsMessage> kafkaCarTemplate;

  public void sendCars(List<CarMessage> cars, MapFragment mapFragment) {
    log.info("Start sending cars from map fragment:" + mapFragment.getMapFragmentId());

    CarsMessage carsMessage = CarsMessage.newBuilder()
        .addAllCarsMessages(cars)
        .build();

    var record = new ProducerRecord<String, CarsMessage>(TopicConfiguration.CARS_TOPIC, mapFragment.getMapFragmentId().toString(), carsMessage);
    ListenableFuture<SendResult<String, CarsMessage>> future = this.kafkaCarTemplate.send(record);

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(SendResult<String, CarsMessage> result) {
      }

      @Override
      public void onFailure(@NotNull Throwable ex) {
        log.error("Error while sending car:" + cars + " " + ex.getMessage());
      }
    });

    log.info("End sending cars from map fragment:" + mapFragment.getMapFragmentId());
  }
}
