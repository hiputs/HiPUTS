package pl.edu.agh.hiputs.communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Route;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.List;
import java.util.stream.Collectors;

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
     * Line location id
     */
    private final String lineId;

    /**
     * Line location position
     */
    private final double positionOnLine;

    /**
     * Serialized route
     */
    private final List<SRouteElement> routeElements;

    private final int currentRoutePosition;


    public SCar (Car realObject) {
        carId = realObject.getId().getValue();
        speed = realObject.getSpeed();
        length = realObject.getLength();
        maxSpeed = realObject.getMaxSpeed();
        lineId = realObject.getLaneId().getValue();
        positionOnLine = realObject.getPositionOnLane();

        currentRoutePosition = realObject.getRouteLocation().getCurrentPosition();
        routeElements = realObject.getRouteLocation()
                .getRoute()
                .getRouteElements()
                .stream()
                .map(routeElement -> new SRouteElement(
                        routeElement.getJunctionId().getValue(),
                        routeElement.getOutgoingLaneId().getValue(),
                        routeElement.getJunctionId().getJunctionType().name()))
                .collect(Collectors.toList());
    }

    @Override
    public Car toRealObject() {

        List<RouteElement> routeElementList = routeElements
                .stream()
                .map(routeElement ->
                    new RouteElement(
                            new JunctionId(routeElement.getJunctionId(), JunctionType.valueOf(routeElement.getJunctionType())),
                            new LaneId(routeElement.getOutgoingLaneId())))
                .collect(Collectors.toList());

        Route route = new Route(routeElementList);
        RouteLocation routeLocation = new RouteLocation(route);
        routeLocation.setCurrentPosition(currentRoutePosition);

        return Car.builder()
                .id(new CarId(carId))
                .length(length)
                .maxSpeed(maxSpeed)
                .routeLocation(routeLocation)
                .speed(speed)
                .laneId(new LaneId(lineId))
                .positionOnLane(positionOnLine)
                .build();
    }
}
