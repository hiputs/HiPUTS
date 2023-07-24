package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerializedCar implements CustomSerializable<Car> {

  /**
   * CarId
   */
  private String carId;

  /**
   * Current speed of car.
   */
  private double speed;

  /**
   * Length of the car.
   */
  private double length;

  /**
   * Maximum possible speed of the car.
   */
  private double maxSpeed;

  /**
   * Lane location id
   */
  private String laneId;

  /**
   * Lane location position
   */
  private double positionOnLane;

  /**
   * Serialized route
   */
  private List<SerializedRouteElement> routeElements;

  /**
   * Serialized currentRoutePosition
   */
  private int currentRoutePosition;

  /**
   * Serialized decision
   */
  private SerializedDecision decision;

  /**
   * Serialized crossroadDecisionProperties
   */
  private SerializedCrossroadDecisionProperties crossroadDecisionProperties;

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
    try {
      // decision = SerializationUtils.serialize(new SerializedDecision(realObject.getDecision()));
      decision = new SerializedDecision(realObject.getDecision());
    } catch (Exception e) {
      log.error("NLP TMP FIXES !!!!");
    }
    crossroadDecisionProperties =
        new SerializedCrossroadDecisionProperties(realObject.getCrossRoadDecisionProperties());
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
        .decision((decision).toRealObject())
        .crossroadDecisionProperties((crossroadDecisionProperties).toRealObject())
        .driver(new Driver(new DriverParameters(ConfigurationService.getConfiguration())))
        .build();
  }
}
