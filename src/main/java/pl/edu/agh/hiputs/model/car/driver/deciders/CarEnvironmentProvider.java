package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarTrailDeciderData;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadSubordination;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CarEnvironmentProvider implements CarProspector {

  double viewRange = 300;

  public double getViewRange() {
    return viewRange;
    //return configurationService.getConfiguration().getCarViewRange();
  }

  public CarFollowingEnvironment getFollowingCar(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    // looking range to previous crossroad
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId prevJunctionId = currentRoad.getIncomingJunctionId();
    Optional<CarReadable> followingCar = currentLane.getCarInBackReadable(currentCar);

    Optional<RoadId> resultRoadId = Optional.of(currentRoad.getRoadId());
    Optional<LaneId> resultLaneId = Optional.of(currentLane.getLaneId());

    double distance;

    distance = currentCar.getPositionOnLane() - currentCar.getLength();
    if (prevJunctionId.isCrossroad() || followingCar.isPresent()) {
      distance -= followingCar.map(CarReadable::getPositionOnLane).orElse(0.0);
    } else {
      int offset = 0;
      RoadId prevRoadId;
      LaneId prevLaneId;
      RoadReadable prevRoad;
      LaneReadable prevLane;

      while (followingCar.isEmpty() && !prevJunctionId.isCrossroad() && distance < getViewRange()) {
        if (offset != 0) {
          distance += currentRoad.getLength();
        }

        Optional<RoadId> prevRoadIdOptional = currentCar.getRouteOffsetRoadId(--offset);
        if (prevRoadIdOptional.isEmpty()) {
          break;
        }

        prevRoadId = prevRoadIdOptional.get();
        prevRoad = roadStructureReader.getRoadReadable(prevRoadId);
        if (prevRoad == null) {
          break;
        }

        prevJunctionId = prevRoad.getIncomingJunctionId();
        Optional<LaneReadable> prevLaneOptional = getPreviousLanes(currentLane, prevRoad, roadStructureReader);
        if (prevLaneOptional.isPresent()) {
          prevLane = prevLaneOptional.get();
          prevLaneId = prevLane.getLaneId();
        } else {
          break;
        }

        followingCar = prevLane.getCarAtExitReadable();
        resultRoadId = Optional.of(prevRoadId);
        resultLaneId = Optional.of(prevLaneId);
        currentLane = prevLane;
        currentRoad = prevRoad;
      }

      if (offset != 0) {
        final RoadReadable finalCurrentRoad = currentRoad;
        distance += followingCar.map(car -> finalCurrentRoad.getLength() - car.getPositionOnLane())
            .orElse(currentLane.getLength());
      }
    }
    return new CarFollowingEnvironment(distance, followingCar, Optional.empty(), resultRoadId, resultLaneId);
  }

  public CarPrecedingEnvironment getPrecedingCar(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    // looking range to preceding car - can be after crossroad
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);

    double distance;

    if (precedingCar.isPresent()) {
      double nextRoadElementPosition =
          precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength());

      distance = nextRoadElementPosition - currentCar.getPositionOnLane();
    } else {
      distance = currentLane.getLength() - currentCar.getPositionOnLane();
      int offset = 0;
      RoadId nextRoadId;
      LaneId nextLaneId;
      RoadReadable nextRoad;
      LaneReadable nextLane;

      // find preceding car on current car route - can be after crossroad
      while (precedingCar.isEmpty() && distance < getViewRange()) {
        if (offset != 0) {
          distance += currentLane.getLength(); // adds previous lane length
        }

        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }

        //Get Next Lanes - can be after crossroad
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          //choose one of available successors for laneId
          nextLane = nextLanes.get(nextLanes.size() - 1);
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          //two cases:
          // - lane is after crossroad (and we are or wrong lane to turn)
          // - narrowing road
          if (currentRoad.getOutgoingJunctionId().isCrossroad()) {
            // We are on the wrong lane take first right lane on road after crossroad
            nextLaneId = nextRoad.getLanes().get(nextRoad.getLanes().size() - 1);
          } else {
            nextLaneId = getNarrowingRoadLaneSuccessor(currentRoad, currentLane.getLaneId(), nextRoad).get();
          }
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.error("getPrecedingCar: There is no available Lanes on Road");
          break;
        }

        precedingCar = nextLane.getCarAtEntryReadable();
        currentLane = nextLane;
        currentRoad = nextRoad;
      }

      //check if the distance between car and next junction is bigger than view range
      if (offset != 0) {
        distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength());
      }
    }
    return new CarPrecedingEnvironment(distance, precedingCar, Optional.empty(), Optional.empty(), Optional.empty());
  }

  public CarPrecedingEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar,
      RoadStructureReader roadStructureReader) {
    // looking range to next crossroad
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);
    Optional<JunctionId> nextCrossroadId;

    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());

    double distance;

    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      double nextRoadElementPosition =
          precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength());

      distance = nextRoadElementPosition - currentCar.getPositionOnLane();
    } else {
      distance = currentLane.getLength() - currentCar.getPositionOnLane();
      int offset = 0;
      RoadId nextRoadId;
      LaneId nextLaneId;
      RoadReadable nextRoad;
      LaneReadable nextLane;

      // find next road element - can be preceding car or junction with type crossroad
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad() && distance < getViewRange()) {
        if (offset != 0) {
          distance += currentLane.getLength(); // adds previous lane length
        }

        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }
        nextJunctionId = nextRoad.getOutgoingJunctionId();

        //Get Next Lanes - can be after crossroad
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          //choose one of available successors for laneId
          nextLane = nextLanes.get(nextLanes.size() - 1);
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          //two cases:
          // - lane is after crossroad (and we are or wrong lane to turn)
          // - narrowing road
          if (currentRoad.getOutgoingJunctionId().isCrossroad()) {
            // We are on the wrong lane take first right lane on road after crossroad
            nextLaneId = nextRoad.getLanes().get(nextRoad.getLanes().size() - 1);
          } else {
            nextLaneId = getNarrowingRoadLaneSuccessor(currentRoad, currentLane.getLaneId(), nextRoad).get();
          }
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.error("getPrecedingCarOrCrossroad: There is no available Lanes on Road");
          break;
        }

        precedingCar = nextLane.getCarAtEntryReadable();
        incomingRoadId = Optional.of(nextRoadId);
        incomingLaneId = Optional.of(nextLaneId);
        currentLane = nextLane;
        currentRoad = nextRoad;
      }

      //check if the distance between car and next junction is bigger than view range
      if (offset != 0) {
        distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength());
      }
    }

    double crossroadDistance = distance;
    if (nextJunctionId.isCrossroad() && crossroadDistance <= getViewRange()
        && roadStructureReader.getJunctionReadable(nextJunctionId) != null) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingRoadId = Optional.empty();
      incomingLaneId = Optional.empty();
    }
    return new CarPrecedingEnvironment(distance, precedingCar, nextCrossroadId, incomingRoadId, incomingLaneId);
  }

  public CarPrecedingEnvironment getPrecedingCrossroad(CarReadable currentCar,
      RoadStructureReader roadStructureReader) {
    // looking range to next crossroad
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<JunctionId> nextCrossroadId;

    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());

    double distance;

    if (nextJunctionId.isCrossroad()) {
      double nextRoadElementPosition = currentLane.getLength();

      distance = nextRoadElementPosition - currentCar.getPositionOnLane();
    } else {
      distance = currentLane.getLength() - currentCar.getPositionOnLane();
      int offset = 0;
      RoadId nextRoadId;
      LaneId nextLaneId;
      RoadReadable nextRoad;
      LaneReadable nextLane;

      // find junction with type crossroad in view range
      while (!nextJunctionId.isCrossroad() && distance < getViewRange()) {
        if (offset != 0) {
          distance += currentLane.getLength(); // adds previous lane length
        }

        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }
        nextJunctionId = nextRoad.getOutgoingJunctionId();

        //Get Next Lanes - can be after crossroad
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          //choose one of available successors for laneId
          nextLane = nextLanes.get(nextLanes.size() - 1);
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          //two cases:
          // - lane is after crossroad (and we are or wrong lane to turn)
          // - narrowing road
          if (currentRoad.getOutgoingJunctionId().isCrossroad()) {
            // We are on the wrong lane take first right lane on road after crossroad
            nextLaneId = nextRoad.getLanes().get(nextRoad.getLanes().size() - 1);
          } else {
            nextLaneId = getNarrowingRoadLaneSuccessor(currentRoad, currentLane.getLaneId(), nextRoad).get();
          }
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.error("getPrecedingCrossroad: There is no available Lanes on Road");
          break;
        }

        incomingRoadId = Optional.of(nextRoadId);
        incomingLaneId = Optional.of(nextLaneId);
        currentLane = nextLane;
        currentRoad = nextRoad;
      }

      //check if the distance between car and next junction is bigger than view range
      if (offset != 0) {
        distance += currentLane.getLength();
      }
    }

    double crossroadDistance = distance;
    if (nextJunctionId.isCrossroad() && crossroadDistance <= getViewRange()
        && roadStructureReader.getJunctionReadable(nextJunctionId) != null) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingRoadId = Optional.empty();
      incomingLaneId = Optional.empty();
    }
    return new CarPrecedingEnvironment(distance, Optional.empty(), nextCrossroadId, incomingRoadId, incomingLaneId);
  }

  public List<LaneReadable> getNextLanes(LaneReadable currentLane, RoadId nextRoadId,
      RoadStructureReader roadStructureReader) {
    return currentLane.getLaneSuccessors()
        .stream()
        .map(roadStructureReader::getLaneReadable)
        .filter(lane -> lane.getRoadId().equals(nextRoadId))
        .toList();
  }

  public List<LaneReadable> getCorrectIncomingLanes(RoadReadable incomingRoad, RoadId nextRoadId,
      RoadStructureReader roadStructureReader) {
    List<LaneReadable> correctLanes = new ArrayList<>();

    incomingRoad.getLanes().stream().map(roadStructureReader::getLaneReadable).forEach(lane -> {
      List<LaneReadable> successors = this.getNextLanes(lane, nextRoadId, roadStructureReader);

      if (!successors.isEmpty()) {
        correctLanes.add(lane);
      }

    });

    return correctLanes;
  }

  public Optional<LaneReadable> getPreviousLanes(LaneReadable currentLane, RoadReadable prevRoad,
      RoadStructureReader roadStructureReader) {
    return prevRoad.getLanes()
        .stream()
        .map(roadStructureReader::getLaneReadable)
        .filter(lane -> lane.getLaneSuccessors().contains(currentLane.getLaneId()))
        .findFirst();
  }

  public Optional<LaneId> getNarrowingRoadLaneSuccessor(RoadReadable currentRoad, LaneId currentLaneId,
      RoadReadable nextRoad) {
    //lane narrowing case - no successors available
    //------------>*-------------->
    //------------>
    //------------>
    List<LaneId> lanes = currentRoad.getLanes();
    int lanePosition = lanes.indexOf(currentLaneId);
    int totalLanes = lanes.size();

    List<LaneId> nextLanes = nextRoad.getLanes();
    int totalNextLanes = nextLanes.size();
    int laneDiff = totalLanes - totalNextLanes;
    if (laneDiff >= 0) {
      if (lanePosition >= 0 && lanePosition < laneDiff) {
        return Optional.of(nextLanes.get(0));
      } else if (lanePosition <= totalLanes - 1 && lanePosition >= totalLanes - laneDiff) {
        return Optional.of(nextLanes.get(totalNextLanes - 1));
      }
    }

    if (lanePosition == 0) {
      return Optional.of(nextLanes.get(0));
    } else if (lanePosition == totalLanes - 1) {
      Optional.of(nextLanes.get(totalNextLanes - 1));
    }

    log.warn("Incorrect lane mapping in graph. Wrong lane mapping for road id: " + currentRoad.getRoadId().getValue());
    return Optional.of(nextLanes.get(totalNextLanes - 1));
  }

  public List<RoadOnJunction> getRightRoadsOnJunction(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId,
      RoadId outgoingRoadId) {
    Optional<RoadOnJunction> incomingRoadOnJunctionOpt = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getRoadId() == incomingRoadId && roadOnJunction.getDirection()
            .equals(RoadDirection.INCOMING))
        .findFirst();
    if (incomingRoadOnJunctionOpt.isEmpty()) {
      return new ArrayList<>();
    }
    RoadOnJunction incomingRoadOnJunction = incomingRoadOnJunctionOpt.get();
    RoadOnJunction outgoingRoadOnJunction = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getRoadId() == outgoingRoadId && roadOnJunction.getDirection()
            .equals(RoadDirection.OUTGOING))
        .findFirst()
        .or(() -> Optional.of(incomingRoadOnJunction))
        .get();

    List<RoadOnJunction> rightRoads;
    rightRoads = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
            && isRoadOnJunctionIndexInRange(roadOnJunction.getRoadIndexOnJunction(),
            incomingRoadOnJunction.getRoadIndexOnJunction(), outgoingRoadOnJunction.getRoadIndexOnJunction()))
        .toList();
    return rightRoads;
  }

  public List<RoadId> getConflictRoadIds(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId,
      RoadId outgoingRoadId, CarId currentCarId, RoadStructureReader roadStructureReader) {
    Optional<RoadOnJunction> incomingRoadOnJunctionOpt = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getRoadId() == incomingRoadId && roadOnJunction.getDirection()
            .equals(RoadDirection.INCOMING))
        .findFirst();
    if (incomingRoadOnJunctionOpt.isEmpty()) {
      return new ArrayList<>();
    }
    RoadOnJunction incomingRoadOnJunction = incomingRoadOnJunctionOpt.get();
    RoadOnJunction outgoingRoadOnJunction = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getRoadId() == outgoingRoadId && roadOnJunction.getDirection()
            .equals(RoadDirection.OUTGOING))
        .findFirst()
        .or(() -> Optional.of(incomingRoadOnJunction))
        .get();

    Stream<RoadOnJunction> conflictRoads;
    conflictRoads = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
            && isRoadOnJunctionIndexInRange(roadOnJunction.getRoadIndexOnJunction(),
            incomingRoadOnJunction.getRoadIndexOnJunction(), outgoingRoadOnJunction.getRoadIndexOnJunction()))
        .toList()
        .stream();

    if (incomingRoadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD)) {
      conflictRoads =
          conflictRoads.filter(roadOnJunction -> roadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD));
    } else {
      conflictRoads = Stream.concat(conflictRoads, roadOnJunctions.stream()
          .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
              && roadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD))).distinct();
    }

    return conflictRoads.map(RoadOnJunction::getRoadId)
        .filter(road -> filterActiveRoad(road, currentCarId, roadStructureReader))
        .toList();
  }

  private boolean filterActiveRoad(RoadId roadId, CarId currentCarId, RoadStructureReader roadStructureReader) {
    RoadReadable road = roadId.getReadable(roadStructureReader);
    // Optional<CarReadable> conflictCarOptional = Optional.empty();
    if (road != null) {
      List<Optional<CarReadable>> conflictCarsOptional = roadStructureReader.getLanesReadableFromRoadId(roadId)
          .stream()
          .map(l -> l.streamCarsFromExitReadable().findFirst())
          .toList();

      for (Optional<CarReadable> car : conflictCarsOptional) {
        if (car.isPresent() && (car.get().getCrossRoadDecisionProperties().isEmpty() || car.get()
            .getCrossRoadDecisionProperties()
            .get()
            .getGiveWayVehicleId()
            .isEmpty() || !car.get()
            .getCrossRoadDecisionProperties()
            .get()
            .getGiveWayVehicleId()
            .get()
            .equals(currentCarId))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isRoadOnJunctionIndexInRange(int index, int incoming, int outgoing) {
    if (incoming <= outgoing) {
      return index > incoming && index < outgoing;
    } else {
      return index > incoming || index < outgoing;
    }
  }

  public List<CarBasicDeciderData> getFirstCarsFromRoads(List<RoadId> conflictRoads,
      RoadStructureReader roadStructureReader) {
    List<CarBasicDeciderData> conflictCars = new ArrayList<>();
    for (RoadId roadId : conflictRoads) {
      RoadReadable road = roadId.getReadable(roadStructureReader);

      if (road != null) {

        List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);
        for (LaneReadable lane : lanes) {
          Optional<CarReadable> conflictCarOptional = lane.streamCarsFromExitReadable().findFirst();
          if (conflictCarOptional.isPresent()) {
            CarReadable conflictCar = conflictCarOptional.get();
            double distance = lane.getLength() - conflictCar.getPositionOnLane();
            conflictCars.add(new CarBasicDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength()));
          }
        }
      }
    }
    return conflictCars;
  }

  public List<CarReadable> getAllFirstCarsFromRoads(List<RoadId> roads, RoadStructureReader roadStructureReader) {
    List<CarReadable> cars = new ArrayList<>();

    for (RoadId roadId : roads) {
      List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);

      for (LaneReadable lane : lanes) {
        List<CarReadable> carsFromExit = lane.streamCarsFromExitReadable().toList();
        if (!carsFromExit.isEmpty()) {
          cars.add(carsFromExit.stream().findFirst().get());
        }
      }
    }
    return cars;
  }

  public List<CarTrailDeciderData> getAllConflictCarsFromRoads(List<RoadId> conflictRoads,
      RoadStructureReader roadStructureReader, double conflictAreaLength) {
    List<CarTrailDeciderData> conflictCars = new ArrayList<>();

    for (RoadId roadId : conflictRoads) {
      RoadReadable road = roadId.getReadable(roadStructureReader);
      if (road != null) {
        double maxSpeed =
            Double.MAX_VALUE;//road.getMaxSpeed(); // #TODO Get max speed from road when it will be available

        List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);
        for (LaneReadable l : lanes) {
          Stream<CarReadable> carsFromExit = l.streamCarsFromExitReadable();

          if (carsFromExit.findAny().isPresent()) {
            final CarId finalFirstCarId = carsFromExit.findFirst().get().getCarId();

            carsFromExit.forEach(conflictCar -> {
              double distance = road.getLength() - conflictCar.getPositionOnLane() - conflictAreaLength / 2;
              conflictCars.add(new CarTrailDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength(),
                  conflictCar.getAcceleration(), Math.min(maxSpeed, conflictCar.getMaxSpeed()), finalFirstCarId,
                  road.getRoadId(), conflictCar.getRouteOffsetRoadId(1)));
            });
          }
        }
      }

      /*if(road.getLength() < getViewRange()){ //does not give any improvement
        double distanceAdd = road.getLength();
        JunctionId previousJunctionId = road.getIncomingJunctionId();
        JunctionReadable junction = roadStructureReader.getJunctionReadable(previousJunctionId);
        List<LaneId> lanesBefore = junction.streamLanesOnJunction().filter(l -> l.getDirection().equals(LaneDirection
        .INCOMING)).map(l->l.getLaneId()).toList();
        for (LaneId prevLaneId: lanesBefore) {
          LaneReadable prevLane = prevLaneId.getReadable(roadStructureReader);
          maxSpeed = Double.MAX_VALUE;//road.getMaxSpeed();
          carsFromExit = prevLane.streamCarsFromExitReadable().toList();
          if (!carsFromExit.isEmpty()) {
            if (firstCarId == null) {
              firstCarId = carsFromExit.stream().findFirst().get().getCarId();
            }
            for (CarReadable conflictCar : carsFromExit) {
              double distance =
                  distanceAdd + prevLane.getLength() - conflictCar.getPositionOnLane() - conflictAreaLength / 2;
              conflictCars.add(new CarTrailDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength(),
                  conflictCar.getAcceleration(), Math.min(maxSpeed, conflictCar.getMaxSpeed()), firstCarId, road
                  .getLaneId(), conflictCar.getRouteOffsetLaneId(1)));
            }
          }
        }
      }*/
    }
    return conflictCars;
  }

  public RoadId getNextOutgoingRoad(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader) {
    RoadId outgoingRoadId = null;
    int offset = 0;
    RoadId tmpRoadId;
    do {
      Optional<RoadId> nextRoadIdOptional = car.getRouteOffsetRoadId(offset++);
      if (nextRoadIdOptional.isPresent()) {
        tmpRoadId = nextRoadIdOptional.get();
        RoadReadable tmpRoad = tmpRoadId.getReadable(roadStructureReader);
        if (tmpRoad != null && tmpRoad.getIncomingJunctionId().equals(junctionId)) {
          outgoingRoadId = tmpRoadId;
        }
      } else {
        tmpRoadId = null;
      }
    } while (outgoingRoadId == null && tmpRoadId != null);
    return outgoingRoadId;
  }
}
