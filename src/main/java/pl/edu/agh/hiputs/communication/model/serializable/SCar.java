package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.Route;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@Builder
@AllArgsConstructor
public class SCar implements CustomSerializable<Car> {

  /**
   * CarId
   */
  private final String carId;

  /**
   * Current speed of car.
   */
  private final double speed;

  /**
   * Length of the car.
   */
  private final double length;

  /**
   * Maximum possible speed of the car.
   */
  private final double maxSpeed;

  /**
   * Lane location id
   */
  private final String laneId;

  /**
   * Lane location position
   */
  private final double positionOnLane;

  /**
   * Serialized route
   */
  private final List<SRouteElement> routeElements;

  private final int currentRoutePosition;

  public SCar(CarEditable realObject) {
    carId = realObject.getCarId().getValue();
    speed = realObject.getSpeed();
    length = realObject.getLength();
    maxSpeed = realObject.getMaxSpeed();
    laneId = realObject.getLaneId().getValue();
    positionOnLane = realObject.getPositionOnLane();

    currentRoutePosition = realObject.getRouteLocation().getCurrentPosition();
    routeElements = realObject.getRouteLocation()
        .getRoute()
        .getRouteElements()
        .stream()
        .map(routeElement -> new SRouteElement(routeElement.getJunctionId().getValue(),
            routeElement.getOutgoingLaneId().getValue(), routeElement.getJunctionId().getJunctionType().name()))
        .collect(Collectors.toList());
  }

  @Override
  public Car toRealObject() {

    List<RouteElement> routeElementList = routeElements.stream()
        .map(routeElement -> new RouteElement(
            new JunctionId(routeElement.getJunctionId(), JunctionType.valueOf(routeElement.getJunctionType())),
            new LaneId(routeElement.getOutgoingLaneId())))
        .collect(Collectors.toList());

    Route route = new Route(routeElementList);
    RouteLocation routeLocation = new RouteLocation(route);
    routeLocation.setCurrentPosition(currentRoutePosition);

    return Car.builder()
        .carId(new CarId(carId))
        .length(length)
        .maxSpeed(maxSpeed)
        .routeLocation(routeLocation)
        .speed(speed)
        .laneId(new LaneId(laneId))
        .positionOnLane(positionOnLane)
        .build();
  }
}
