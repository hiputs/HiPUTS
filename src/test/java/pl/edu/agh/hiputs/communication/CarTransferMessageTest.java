package pl.edu.agh.hiputs.communication;

import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.communication.model.serializable.SRouteElement;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Route;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CarTransferMessageTest {

    @Test
    void toRealObject(){
        //given
        SCar sCar = getSerializedCar();

        //when
        Car car = sCar.toRealObject();

        //then
        assertThat(car)
                .usingRecursiveComparison()
                .isEqualTo(getCar());
    }

    @Test
    void toSerializedObject(){
        //given
        Car car = getCar();

        //when
        SCar sCar = new SCar(car);

        //then
        assertThat(sCar)
                .usingRecursiveComparison()
                .isEqualTo(getSerializedCar());
    }

    private Car getCar(){
        List<RouteElement> routeElementList = List.of(
                new RouteElement(new JunctionId("zxc", JunctionType.BEND), new LaneId("vbn")),
                new RouteElement(new JunctionId("zxc1", JunctionType.BEND), new LaneId("vbn1"))
        );

        Route route = new Route(routeElementList);
        return Car.builder()
                .id(new CarId("12345"))
                .length(12)
                .speed(13)
                .maxSpeed(14)
                .laneId(new LaneId("abc"))
                .positionOnLane(0)
                .routeLocation(new RouteLocation(route))
                .build();
    }

    private SCar getSerializedCar(){
        List<SRouteElement> routeElementList = List.of(
                new SRouteElement("zxc", "vbn", "BEND"),
                new SRouteElement("zxc1", "vbn1", "BEND")
        );

        return SCar.builder()
                .carId("12345")
                .length(12)
                .speed(13)
                .maxSpeed(14)
                .laneId("abc")
                .positionOnLane(0)
                .routeElements(routeElementList)
                .build();
    }
}
