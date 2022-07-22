package pl.edu.agh.hiputs.communication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedDecision;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedRouteElement;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

public class CarTransferMessageTest {

  @Test
  void toRealObject() {
    //given
    SerializedCar serializedCar = getSerializedCar();

    //when
    Car car = serializedCar.toRealObject();

    //then
    assertThat(car).usingRecursiveComparison().isEqualTo(getCar());
  }

  @Test
  void toSerializedObject() {
    //given
    Car car = getCar();

    //when
    SerializedCar serializedCar = new SerializedCar(car);

    //then
    assertThat(serializedCar).usingRecursiveComparison().isEqualTo(getSerializedCar());
  }

  private Car getCar() {
    List<RouteElement> routeElementList =
        List.of(new RouteElement(new JunctionId("zxc", JunctionType.BEND), new LaneId("vbn")),
            new RouteElement(new JunctionId("zxc1", JunctionType.BEND), new LaneId("vbn1")));

    RouteWithLocation route = new RouteWithLocation(routeElementList, 0);
    return Car.builder()
        .carId(new CarId("12345"))
        .length(12)
        .speed(13)
        .maxSpeed(14)
        .laneId(new LaneId("abc"))
        .positionOnLane(0)
        .routeWithLocation(route)
        .decision(Decision.builder().laneId(new LaneId("1111")).build())
        .build();
  }

  private SerializedCar getSerializedCar() {
    List<SerializedRouteElement> routeElementList =
        List.of(new SerializedRouteElement("zxc", "vbn", "BEND"), new SerializedRouteElement("zxc1", "vbn1", "BEND"));

    return SerializedCar.builder()
        .carId("12345")
        .length(12)
        .speed(13)
        .maxSpeed(14)
        .laneId("abc")
        .positionOnLane(0)
        .routeElements(routeElementList)
        .decision(SerializedDecision.builder().laneId("1111").build())
        .build();
  }
}
