package pl.edu.agh.hiputs.communication.model.serializable;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
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
  private final String roadId;

  /**
   * Lane location position
   */
  private final double positionOnRoad;

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
  private byte[] decision;

  /**
   * Serialized crossroadDecisionProperties
   */
  private final byte[] crossroadDecisionProperties;

  public SerializedCar(CarEditable realObject) {
    carId = realObject.getCarId().getValue();
    speed = realObject.getSpeed();
    length = realObject.getLength();
    maxSpeed = realObject.getMaxSpeed();
    roadId = realObject.getRoadId().getValue();
    positionOnRoad = realObject.getPositionOnRoad();

    currentRoutePosition = realObject.getRouteWithLocation().getCurrentPosition();
    routeElements = realObject.getRouteWithLocation()
        .getRouteElements()
        .stream()
        .map(routeElement -> new SerializedRouteElement(routeElement.getJunctionId().getValue(),
            routeElement.getOutgoingRoadId().getValue(), routeElement.getJunctionId().getJunctionType().name()))
        .collect(Collectors.toList());
    try {
      decision = SerializationUtils.serialize(new SerializedDecision(realObject.getDecision()));
    } catch (Exception e) {
      log.error("NLP TMP FIXES !!!!");
    }
    crossroadDecisionProperties = SerializationUtils.serialize(new SerializedCrossroadDecisionProperties(realObject.getCrossRoadDecisionProperties()));
  }

  @Override
  public Car toRealObject() {

    List<RouteElement> routeElementList = routeElements.stream()
        .map(routeElement -> new RouteElement(
            new JunctionId(routeElement.getJunctionId(), JunctionType.valueOf(routeElement.getJunctionType())),
            new RoadId(routeElement.getOutgoingRoadId())))
        .collect(Collectors.toList());

    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElementList, currentRoutePosition);

    return Car.builder()
        .carId(new CarId(carId))
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .speed(speed)
        .roadId(new RoadId(roadId))
        .positionOnRoad(positionOnRoad)
        .decision(((SerializedDecision)SerializationUtils.deserialize(decision)).toRealObject())
        .crossroadDecisionProperties(((SerializedCrossroadDecisionProperties) SerializationUtils.deserialize(crossroadDecisionProperties)).toRealObject())
        .driver(new Driver(new DriverParameters(ConfigurationService.getConfiguration())))
        .build();
  }
}
