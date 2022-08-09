package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Getter
@Builder
@AllArgsConstructor
public class SerializedLane implements CustomSerializable<List<Car>> {

  String laneId;
  List<SerializedCar> cars;

  public SerializedLane(LaneEditable lane) {
    cars = lane.streamCarsFromEntryEditable().map(SerializedCar::new).collect(Collectors.toList());
    laneId = lane.getLaneId().getValue();
  }

  @Override
  public List<Car> toRealObject() {
    return cars.stream().map(SerializedCar::toRealObject).collect(Collectors.toList());
  }
}
