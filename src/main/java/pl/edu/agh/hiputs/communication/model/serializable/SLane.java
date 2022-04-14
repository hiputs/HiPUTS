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
public class SLane implements CustomSerializable<List<Car>> {

  String laneId;
  List<SCar> cars;

  public SLane(LaneEditable lane) {
    cars = lane.streamCarsFromExitEditable().map(SCar::new).collect(Collectors.toList());
    laneId = lane.getLaneId().getValue();
  }

  @Override
  public List<Car> toRealObject() {
    return cars.stream().map(SCar::toRealObject).collect(Collectors.toList());
  }
}
