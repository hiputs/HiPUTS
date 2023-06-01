package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class SerializedLane implements CustomSerializable<List<Car>> {

  String laneId;
  List<SerializedCar> cars;

  public SerializedLane(LaneEditable lane) {
    cars = getCars(lane);
    laneId = lane.getLaneId().getValue();
  }

  private List<SerializedCar> getCars(LaneEditable lane) {
    try {
      return lane.streamCarsFromEntryEditable().map(SerializedCar::new).collect(Collectors.toList());
    } catch (Exception e){
      log.warn("Error get Cars from lane", e);
      return List.of();
    }
  }

  @Override
  public List<Car> toRealObject() {
    return cars.stream()
        .map(car -> {
          try {
            return car.toRealObject();
          } catch(Exception e){
            log.warn("Car nlp occurred, car will be removed");
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
