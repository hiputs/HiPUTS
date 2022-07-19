package pl.edu.agh.hiputs.communication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.communication.model.serializable.SDecision;
import pl.edu.agh.hiputs.communication.model.serializable.SRouteElement;
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
    SCar sCar = getSerializedCar();

    //when
    Car car = sCar.toRealObject();

    //then
    assertThat(car).usingRecursiveComparison().isEqualTo(getCar());
  }

  @Test
  void toSerializedObject() {
    //given
    Car car = getCar();

    //when
    SCar sCar = new SCar(car);

    //then
    assertThat(sCar).usingRecursiveComparison().isEqualTo(getSerializedCar());
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

  private SCar getSerializedCar() {
    List<SRouteElement> routeElementList =
        List.of(new SRouteElement("zxc", "vbn", "BEND"), new SRouteElement("zxc1", "vbn1", "BEND"));

    return SCar.builder()
        .carId("12345")
        .length(12)
        .speed(13)
        .maxSpeed(14)
        .laneId("abc")
        .positionOnLane(0)
        .routeElements(routeElementList)
        .decision(SDecision.builder().laneId("1111").build())
        .build();
  }
}
