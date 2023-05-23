package pl.edu.agh.hiputs.service.pathfinder;


import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.ConfigurationService;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class CarProvider {
    private final Configuration configuration;

    public CarProvider() {
        this.configuration = ConfigurationService.getConfiguration();
    }

    private Double getDefaultCarLength() {
        return 4.5; //DEFAULT_CAR_LENGTH;
    }

    private Double getDefaultMaxSpeed() {
        return configuration.getDefaultMaxSpeed();
    }

    private Double getMaxDeceleration() {
        return configuration.getMaxDeceleration();
    }

    private Double getTimeStep() {
        return configuration.getSimulationTimeStep();
    }

    private Double getDefaultMaxSpeedSecurityFactor() {
        return 0.8; //DEFAULT_MAX_SPEED_SECURITY_FACTOR;
    }

    public Car generateCar(MapFragment mapFragment, RouteWithLocation routeWithLocation) {
        LaneId startLaneId = routeWithLocation.getRouteElements().get(0).getOutgoingLaneId();
        double position = ThreadLocalRandom.current().nextDouble(0, mapFragment.getLaneReadable(startLaneId).getLength());
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        return Car.builder()
                .length(getDefaultCarLength())
                .maxSpeed(getDefaultMaxSpeed())
                .routeWithLocation(routeWithLocation)
                .laneId(startLaneId)
                .positionOnLane(position)
                .speed(threadLocalRandom.nextDouble(getDefaultMaxSpeed()))
                .driver(new Driver(new DriverParameters(configuration)))
                .build();
    }


    public void limitSpeedPreventCollisionOnStart(Car currentCar, LaneEditable lane) {
        Optional<CarReadable> carAtEntryOptional = lane.getCarAtEntryReadable();
        double distance;
        if(carAtEntryOptional.isPresent()) {
            CarReadable carAtEntry = carAtEntryOptional.get();
            double brakingDistance = carAtEntry.getSpeed() * carAtEntry.getSpeed() / getMaxDeceleration() / 2;
            distance = carAtEntry.getPositionOnLane() - carAtEntry.getLength() + brakingDistance;
        }
        else{
            distance = lane.getLength();
        }
        distance -=  currentCar.getPositionOnLane();

        double maxSpeed = 0.0;
        //Limit maxSped cause car need to stop in integer number of time steps
        if(distance > 0){
            maxSpeed = Math.sqrt(distance * 2 * getMaxDeceleration()) * getDefaultMaxSpeedSecurityFactor();
            double timeToStop = maxSpeed / getMaxDeceleration();
            timeToStop -= timeToStop % getTimeStep();
            maxSpeed = timeToStop * getMaxDeceleration() * getDefaultMaxSpeedSecurityFactor();
        }

        if (currentCar.getSpeed() > maxSpeed){
            log.debug("Car: " + currentCar.getCarId() + " has reduced its speed before start from: " + currentCar.getSpeed() + " to: " + maxSpeed + ", distance: " + distance);
            currentCar.setSpeed(maxSpeed);
        }
    }
}
