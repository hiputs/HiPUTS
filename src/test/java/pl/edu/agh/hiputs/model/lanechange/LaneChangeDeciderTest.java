package pl.edu.agh.hiputs.model.lanechange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarEnvironmentProvider;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.ICarFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IdmDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.ILaneChangeChecker;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.ILaneChangeDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.LaneChangeDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.LaneChangeDecision;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.MobilModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger.MobilModelDecision;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

public class LaneChangeDeciderTest {
  ICarFollowingModel idm;
  CarEnvironmentProvider prospector;
  FunctionalDecider idmDecider;
  ILaneChangeChecker mobilModel;
  ILaneChangeDecider laneChangeDecider;

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
    this.laneChangeDecider = new LaneChangeDecider(prospector, new MobilModel(idmDecider, prospector));
    this.modelMobilTestData = ModelMobilTestData.prepareTestData(road_length);
  }

  @Test
  void mandatoryLaneChange() {
    /**
     *        PATCH2
     *       road3            road4
     *    ----[currentCar]----> * -------------->*
     *    -------------------->   -------------->
     *    2 Bend         3 Crossroad     4 Bend
     *                       \\
     *                        \\  road5
     *                  lane51 \\ lane50
     *                          v
     *                         5 Bend
     */

    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane30 = mapFragment.getLaneEditable(laneId30);
    LaneId laneId31 = modelMobilTestData.getLaneId(31);
    CarEditable currentCar = createCar(
        laneId30,
        lane30.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements2, 2),
        road_length - 150,
        4.0);
    lane30.addCarAtEntry(currentCar);

    //when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //then
    assertEquals(laneId31, decision.getTargetLaneId());
    assertEquals(2.0, decision.getAcceleration().get(), 0.02);
  }

  @Test
  void noMandatoryLaneChange() {
    /**
     *        PATCH2
     *       road3            road4
     *    --------------------> * -------------->*
     *    ----[currentCar]---->   -------------->
     *    2 Bend         3 Crossroad     4 Bend
     *                       \\
     *                        \\  road5
     *                  lane51 \\ lane50
     *                          v
     *                         5 Bend
     */

    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId31 = modelMobilTestData.getLaneId(31);
    LaneEditable lane31 = mapFragment.getLaneEditable(laneId31);

    CarEditable currentCar = createCar(
        laneId31,
        lane31.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements2, 2),
        road_length - 150,
        4.0);
    lane31.addCarAtEntry(currentCar);

    //when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //then
    assertEquals(laneId31, decision.getTargetLaneId());
    assertEquals(Optional.empty(), decision.getAcceleration());
  }

  @Test
  void mandatoryLaneChangeInNoLaneChangeZone() {
     /**
     *        PATCH2
     *       road3            road4
     *    ----[currentCar]----> * -------------->*
     *    -------------------->   -------------->
     *    2 Bend         3 Crossroad     4 Bend
     *                       \\
     *                        \\  road5
     *                  lane51 \\ lane50
     *                          v
     *                         5 Bend
     */

    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane30 = mapFragment.getLaneEditable(laneId30);
    LaneId laneId31 = modelMobilTestData.getLaneId(31);
    CarEditable currentCar = createCar(
        laneId30,
        lane30.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements2, 2),
        road_length - 30,
        4.0);
    lane30.addCarAtEntry(currentCar);

    //when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //then
    assertEquals(laneId31, decision.getTargetLaneId());
    assertTrue(decision.getAcceleration().isPresent());
  }

  @Test
  void mandatoryLaneChangeNewFollowerExists() {
    /**
     *        PATCH2
     *       road3            road4
     *    ------------------[currentCar]----> * -------------->*
     *    --[newFollower]------------------->   -------------->
     *    2 Bend         3 Crossroad     4 Bend
     *                       \\
     *                        \\  road5
     *                  lane51 \\ lane50
     *                          v
     *                         5 Bend
     */

    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId30 = modelMobilTestData.getLaneId(30);
    LaneEditable lane30 = mapFragment.getLaneEditable(laneId30);
    LaneId laneId31 = modelMobilTestData.getLaneId(31);
    LaneEditable lane31 = mapFragment.getLaneEditable(laneId31);

    CarEditable currentCar = createCar(
        laneId30,
        lane30.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements2, 2),
        road_length - 150,
        4.0);
    CarEditable newFollower = createCar(
        laneId31,
        lane31.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements2, 2),
        road_length - 200,
        4.0);

    lane30.addCarAtEntry(currentCar);
    lane31.addCarAtEntry(newFollower);

    //when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //then
    assertEquals(laneId31, decision.getTargetLaneId());
    assertEquals(2.0, decision.getAcceleration().get(), 0.02);
  }

  @Test
  void changeLaneDecisionNoCarsTargetLaneModelMobil () {
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

    //  when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //  then
    assertEquals(laneId20, decision.getTargetLaneId());
    assertEquals(2.0, decision.getAcceleration().get(), 0.02);
  }

  @Test
  void changeLaneDecisionNoCarsTargetLaneInNoLaneChangeZone () {
    /*
     *                road2                                  road3
     * -----------------------------------> * ------------------------------------------->*
     * ----------[followingCar]----------->   ---------[currentCar]----[precedingCar]---->
     *                                                                                CROSSROAD
     *                                             [------------- NO LANE CHANGE ZONE -----]
     *
     * */
    // given
    MapFragment mapFragment = modelMobilTestData.mapFragment;
    LaneId laneId21 = modelMobilTestData.getLaneId(21);
    LaneId laneId31 = modelMobilTestData.getLaneId(31);
    LaneEditable lane21 = mapFragment.getLaneEditable(laneId21);
    LaneEditable lane31 = mapFragment.getLaneEditable(laneId31);
    CarEditable followingCar = createCar(
        laneId21,
        lane21.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 0),
        road_length - 20,
        4.0);
    CarEditable currentCar = createCar(
        laneId31,
        lane31.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        road_length - 20,
        1.0);

    CarEditable precedingCar = createCar(
        laneId31,
        lane31.getRoadId(),
        new RouteWithLocation(modelMobilTestData.routeElements1, 1),
        road_length-6,
        2.0);

    lane21.addCarAtEntry(followingCar);
    lane31.addCarAtEntry(precedingCar);
    lane31.addCarAtEntry(currentCar);

    //  when
    CarPrecedingEnvironment nextCrossroad = prospector.getPrecedingCrossroad(currentCar,mapFragment);
    LaneChangeDecision decision = laneChangeDecider.makeDecision(currentCar,nextCrossroad,mapFragment);

    //  then
    assertEquals(laneId31, decision.getTargetLaneId());
    assertEquals(Optional.empty(), decision.getAcceleration());
  }


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

}
