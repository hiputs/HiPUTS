package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@Builder
@AllArgsConstructor
public class SerializedCar implements CustomSerializable<Car> {

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
  private final List<SerializedRouteElement> routeElements;

  /**
   * Serialized currentRoutePosition
   */
  private final int currentRoutePosition;

  /**
   * Serialized decision
   */
  private final SerializedDecision decision;

  public SerializedCar(CarEditable realObject) {
    carId = realObject.getCarId().getValue();
    speed = realObject.getSpeed();
    length = realObject.getLength();
    maxSpeed = realObject.getMaxSpeed();
    laneId = realObject.getLaneId().getValue();
    positionOnLane = realObject.getPositionOnLane();

    currentRoutePosition = realObject.getRouteWithLocation().getCurrentPosition();
    routeElements = realObject.getRouteWithLocation()
        .getRouteElements()
        .stream()
        .map(routeElement -> new SerializedRouteElement(routeElement.getJunctionId().getValue(),
            routeElement.getOutgoingLaneId().getValue(), routeElement.getJunctionId().getJunctionType().name()))
        .collect(Collectors.toList());
    decision = new SerializedDecision(realObject.getDecision());
  }

  @Override
  public Car toRealObject() {

    List<RouteElement> routeElementList = routeElements.stream()
        .map(routeElement -> new RouteElement(
            new JunctionId(routeElement.getJunctionId(), JunctionType.valueOf(routeElement.getJunctionType())),
            new LaneId(routeElement.getOutgoingLaneId())))
        .collect(Collectors.toList());

    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElementList, currentRoutePosition);

    return Car.builder()
        .carId(new CarId(carId))
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .speed(speed)
        .laneId(new LaneId(laneId))
        .positionOnLane(positionOnLane)
        .decision(decision.toRealObject())
        .build();
  }
}
