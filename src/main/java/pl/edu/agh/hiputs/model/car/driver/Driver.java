package pl.edu.agh.hiputs.model.car.driver;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspectorImpl;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.BasicJunctionDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IdmDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

/**
 * Driver class with an algorithm that will take into consideration all deciders
 * and provide proper interface of a Car for the driver:
 * accelerate, decelerate, change lane to right, change lane to left
 */
@RequiredArgsConstructor
public class Driver implements IDriver {

  private final CarReadable car;
  private final CarProspector prospector = new CarProspectorImpl();
  private final FunctionalDecider idmDecider = new IdmDecider();
  private final FunctionalDecider junctionDecider = new BasicJunctionDecider();

  public Decision makeDecision(RoadStructureReader roadStructureReader) {
    // make local decision based on read only road structure (watch environment) and save it locally

    //First prepare CarEnvironment
    //CarEnvironment environment = this.getPrecedingCar(roadStructureReader);

    double acceleration;
    CarEnvironment environment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);
    if(environment.getPrecedingCar().isPresent()){
      acceleration = idmDecider.makeDecision(car, environment, roadStructureReader);
    }
    else{
      acceleration = junctionDecider.makeDecision(car, environment, roadStructureReader);
    }
    //= this.decider.makeDecision(this, roadStructureReader);

    LaneId currentLaneId = this.car.getLaneId();
    LaneReadable destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
    int offset = 0;
    double desiredPosition = this.calculateFuturePosition(acceleration);
    Optional<LaneId> desiredLaneId;

    while (desiredPosition > destinationCandidate.getLength()) {
      desiredPosition -= destinationCandidate.getLength();
      desiredLaneId = car.getRouteOffsetLaneId(offset + 1);
      if (desiredLaneId.isEmpty()) {
        currentLaneId = null;
        break;
      }
      offset++;
      currentLaneId = desiredLaneId.get();
      destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
    }

    return Decision.builder()
        .acceleration(acceleration)
        .speed(car.getSpeed() + acceleration)
        .laneId(currentLaneId)
        .positionOnLane(desiredPosition)
        .offsetToMoveOnRoute(offset)
        .build();
  }

  private double calculateFuturePosition(double acceleration) {
    return car.getPositionOnLane() + car.getSpeed() + acceleration / 2;
  }
}
