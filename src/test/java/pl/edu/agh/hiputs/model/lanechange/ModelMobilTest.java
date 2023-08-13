package pl.edu.agh.hiputs.model.lanechange;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarEnvironmentProvider;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.ICarFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IdmDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.ILaneChangeChecker;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.MobilModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.MobilModelDecision;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

public class ModelMobilTest {

  ICarFollowingModel idm;
  CarEnvironmentProvider prospector;
  FunctionalDecider idmDecider;
  ILaneChangeChecker mobilModel;

  /* ModelMobilTestData
   *          PATCH1                   \           PATCH2
   *      road1              road2              road3            road4
   * *-----lane10---> * -----lane20---> * -----lane30---> * -----lane40--->*
   *  -----lane11--->   -----lane21--->   -----lane31--->   -----lane41--->
   * 0 Bend           1 Bend            2 Bend             3 Crossroad     4 Bend
   *                                                      \\
   *                                                       \\  road5
   *                                                 lane51 \\ lane50
   *                                                         v
   *                                                          5 Bend
   *
   * lane31 - successors [lane41, lane50, lane51]
   * */
  ModelMobilTestData modelMobilTestData;

  double road_length = 200.0;

  @BeforeEach
  void setup() {
    double distanceHeadway = 2.0;
    double timeHeadway = 2.0;
    double maxAcceleration = 2.0;
    double maxDeceleration = 3.5;

    this.idm = new Idm(distanceHeadway, timeHeadway, maxAcceleration, maxDeceleration);
    this.prospector = new CarEnvironmentProvider();
    this.idmDecider = new IdmDecider(idm);
    this.mobilModel = new MobilModel(this.idmDecider,prospector);
    this.modelMobilTestData = ModelMobilTestData.prepareTestData(road_length);
  }

  @Test
  void changeLaneDecisionNoCarsTargetLane () {
    /*
     *                road1                                  road2
     * -----------------------------------> * ------------------------------------------->*
     * ----------[followingCar]----------->   ---------[currentCar]----[precedingCar]---->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        2,
        1.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(true, decision.isCanChangeLane());
    assertEquals(2.0, decision.getAcceleration().get(), 0.001);
  }

  @Test
  void changeLaneDecisionCarTargetLanePositionEqual () {
    /*
     *                road1                                  road2
     * -----------------------------------> * ---------[adjacentCar]--------------------->*
     * ----------[followingCar]----------->   ---------[currentCar]----[precedingCar]---->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        2,
        1.0);
    CarEditable adjacentCar = createCar(
        laneId20,
        lane20.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane20.addCarAtEntry(adjacentCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(false, decision.isCanChangeLane());
    assertEquals(Optional.empty(), decision.getAcceleration());
  }

  @Test
  void changeLaneDecisionCarOnTargetLaneIncorrectDeceleration() {
    /*
     *                road1                                  road2
     * -----------------------------------> * -[adjacentCar]----------------------------------->*
     * ----------[followingCar]----------->   ---------------[currentCar]----[precedingCar]---->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        1.0);
    CarEditable adjacentCar = createCar(
        laneId20,
        lane20.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        20,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane20.addCarAtEntry(adjacentCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(false, decision.isCanChangeLane());
    assertEquals(Optional.empty(), decision.getAcceleration());
  }

  @Test
  void changeLaneDecisionNewFollowingCarExists() {
    /*
     *                road1                                  road2
     * -----------[newFollowingCar]-------> * ------------------------------------>*
     * ----------[followingCar]----------->   ----[currentCar]--[precedingCar]---->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneId laneId10 = modelMobilTestData.getLaneId(10);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    LaneEditable lane10 = mapFragment.getLaneEditable(laneId10);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        1.0);
    CarEditable newFollowingCar = createCar(
        laneId10,
        lane10.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        20,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane10.addCarAtEntry(newFollowingCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(true, decision.isCanChangeLane());
    assertEquals(2.0, decision.getAcceleration().get(), 0.001);
  }

  @Test
  void changeLaneDecisionNewPrecedingCarExists() {
    /*
     *                road1                                  road2                              road3
     * -----------[newFollowingCar]-------> * ------------------------------------> * -------[newPrecedingCar]------------> *
     * ----------[followingCar]----------->   ----[currentCar]--[precedingCar]---->   ------------------------------------>
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneId laneId10 = modelMobilTestData.getLaneId(10);
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    LaneEditable lane10 = mapFragment.getLaneEditable(laneId10);
    LaneEditable lane30 = mapFragment.getLaneEditable(laneId30);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        1.0);
    CarEditable newFollowingCar = createCar(
        laneId10,
        lane10.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        20,
        2.0);

    CarEditable newPrecedingCar = createCar(
        laneId30,
        lane30.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 2),
        1,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane10.addCarAtEntry(newFollowingCar);
    lane30.addCarAtEntry(newPrecedingCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(true, decision.isCanChangeLane());
    assertEquals(2.0, decision.getAcceleration().get(), 0.001);
  }

  @Test
  void changeLaneDecisionNewPrecedingCarExistsSameRoad() {
    /*
     *                road1                                  road2
     * -----------[newFollowingCar]-------> * ------------------------------[newPrecedingCar]----> *
     * ----------[followingCar]----------->   ----[currentCar]--[precedingCar]------------------->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneId laneId10 = modelMobilTestData.getLaneId(10);
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    LaneEditable lane10 = mapFragment.getLaneEditable(laneId10);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        1.0);
    CarEditable newFollowingCar = createCar(
        laneId10,
        lane10.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        20,
        2.0);

    CarEditable newPrecedingCar = createCar(
        laneId20,
        lane20.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        100,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane10.addCarAtEntry(newFollowingCar);
    lane20.addCarAtEntry(newPrecedingCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(true, decision.isCanChangeLane());
    assertEquals(2.0, decision.getAcceleration().get(), 0.01);
  }

  @Test
  void changeLaneDecisionNewPrecedingCarExistsSameRoadTooClose() {
    /*
     *                road1                                  road2
     * -----------[newFollowingCar]-------> * --------------------[newPrecedingCar]----> *
     * ----------[followingCar]----------->   ----[currentCar]--[precedingCar]--------->
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId11 = modelMobilTestData.getLaneId(11);
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId20 = modelMobilTestData.getLaneId(20);
    LaneId laneId10 = modelMobilTestData.getLaneId(10);
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane11 = mapFragment.getLaneEditable(laneId11);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane20 = mapFragment.getLaneEditable(laneId20);
    LaneEditable lane10 = mapFragment.getLaneEditable(laneId10);
    CarEditable followingCar = createCar(
        laneId11,
        lane11.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        10,
        1.0);
    CarEditable newFollowingCar = createCar(
        laneId10,
        lane10.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        2,
        10.0);

    CarEditable precedingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        20,
        2.0);

    CarEditable newPrecedingCar = createCar(
        laneId20,
        lane20.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        21,
        2.0);

    lane11.addCarAtEntry(followingCar);
    lane21.addCarAtEntry(precedingCar);
    lane21.addCarAtEntry(currentCar);
    lane10.addCarAtEntry(newFollowingCar);
    lane20.addCarAtEntry(newPrecedingCar);

    double politeness = 1.0;

    //  when
    MobilModelDecision decision = mobilModel.makeDecision(currentCar, lane20.getLaneId(), politeness, mapFragment);

    //  then
    assertEquals(false, decision.isCanChangeLane());
    assertEquals(Optional.empty(), decision.getAcceleration());
  }

  // @Test
  // void x() {
  //   MapFragment mapFragment =  modelMobilTestData.mapFragment;
  //
  //   LaneId laneId = modelMobilTestData.getLaneId(10);
  //   LaneId laneId1 = modelMobilTestData.getLaneId(11);
  //   LaneEditable lane = mapFragment.getLaneEditable(laneId);
  //   LaneEditable lane1 = mapFragment.getLaneEditable(laneId1);
  //
  //
  //   CarEditable car1 = createCar(
  //       laneId,
  //       lane.getRoadId(),
  //       new RouteWithLocation(modelMobilTestData.routeElements1,0),
  //       0,
  //       10.0,
  //       laneId1
  //   );
  //
  //   lane.addNewCar(car1);
  //
  //   Decision decision = car1.getDecision();
  //   LaneId prevLaneId = laneId;
  //
  //   if (decision.getRoadId() != null && !prevLaneId.equals(decision.getLaneId())) {
  //     LaneEditable destinationLane = mapFragment.getLaneEditable(decision.getLaneId());
  //     destinationLane.addIncomingCar(car1);
  //   }
  //
  //   try {
  //     List<CarEditable> carsToRemove = lane.streamCarsFromExitEditable()
  //         .filter(car -> !Objects.equals(car.getDecision().getLaneId(), lane.getLaneId()) || car.update().isEmpty())
  //         .collect(Collectors.toList());
  //     for (CarEditable car : carsToRemove) {
  //       lane.removeCar(car);
  //       //If remove instance which stay on old road draw warning
  //     }
  //   }catch (Exception e) {
  //
  //   }
  //   lane1 = mapFragment.getLaneEditable(laneId1);
  //
  //
  // }

  private CarEditable createCar(LaneId laneId, RoadId roadId, RouteWithLocation route, double position, double speed) {
    return Car.builder()
        .carId(CarId.random())
        .laneId(laneId)
        .roadId(roadId)
        .routeWithLocation(route)
        .positionOnLane(position)
        .speed(speed)
        .build();
  }

  // private CarEditable createCar(LaneId laneId, RoadId roadId, RouteWithLocation route, double position, double speed, LaneId laneId2) {
  //   return Car.builder()
  //       .carId(CarId.random())
  //       .laneId(laneId)
  //       .roadId(roadId)
  //       .routeWithLocation(route)
  //       .positionOnLane(position)
  //       .speed(speed)
  //       .decision(Decision.builder().speed(100.0).acceleration(2.0).offsetToMoveOnRoute(0).positionOnRoad(0).roadId(roadId).laneId(laneId2).build())
  //       .build();
  // }
}
