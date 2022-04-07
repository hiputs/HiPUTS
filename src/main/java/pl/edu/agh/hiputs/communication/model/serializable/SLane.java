package pl.edu.agh.hiputs.communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class SLane implements CustomSerializable<List<Car>> {

    String lineId;
    List<SCar> cars;

    public SLane(LaneReadWrite line) {
        cars = line.getAllCars()
                .stream()
                .map(SCar::new)
                .collect(Collectors.toList());
        lineId = line.getId().getValue();
    }

    @Override
    public List<Car> toRealObject() {
        return cars.stream()
                .map(SCar::toRealObject)
                .collect(Collectors.toList());
    }
}
