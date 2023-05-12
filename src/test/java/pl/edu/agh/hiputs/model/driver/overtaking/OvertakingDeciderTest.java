package pl.edu.agh.hiputs.model.driver.overtaking;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspectorImpl;
import pl.edu.agh.hiputs.model.car.driver.deciders.overtaking.OvertakingDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.overtaking.OvertakingEnvironment;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

public class OvertakingDeciderTest {

  private MapFragment mapFragment;
  private Car carA, carB, carC, carD;
  private RoadEditable startLane, oppositeLane;
  private CarProspector prospector = new CarProspectorImpl();

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMapForOvertaking();
    startLane = mapFragment.getLocalRoadIds()
        .stream()
        .map(laneId -> mapFragment.getRoadEditable(laneId))
        .filter(laneEditable -> laneEditable.getLeftNeighbor().isPresent() && laneEditable.getLeftNeighbor()
            .get()
            .getHorizontalSign()
            .equals(HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE))
        .findFirst()
        .get();
    oppositeLane = mapFragment.getRoadEditable(startLane.getLeftNeighbor().get().getRoadId());
    carA = null;
    carB = null;
    carC = null;
    carD = null;
  }

  @Test
  public void getPositiveOvertakingDecisionOnlyPrecedingCar() {
    carA = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(0).maxSpeed(25).speed(18).acceleration(1).build();
    carB =
        Car.builder().roadId(startLane.getRoadId()).positionOnRoad(6).maxSpeed(16).speed(15).acceleration(0.1).build();
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    boolean decision = decider.overtakeDecision(carA, carEnvironment, mapFragment);
    Assertions.assertTrue(decision);
  }

  @Test
  public void getPositiveOvertakingDecisionCarOnOppositeLane() {
    carA = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(0).maxSpeed(25).speed(18).acceleration(1).build();
    carB =
        Car.builder().roadId(startLane.getRoadId()).positionOnRoad(6).maxSpeed(16).speed(15).acceleration(0.1).build();
    carD = Car.builder()
        .roadId(oppositeLane.getRoadId())
        .positionOnRoad(oppositeLane.getLength() - 300)
        .maxSpeed(20)
        .speed(16)
        .acceleration(0.1)
        .build();
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    boolean decision = decider.overtakeDecision(carA, carEnvironment, mapFragment);
    Assertions.assertTrue(decision);
  }

  @Test
  public void getNegativeOvertakingDecisionCarOnOppositeLaneIsToClose() {
    carA = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(0).maxSpeed(25).speed(18).acceleration(1).build();
    carB =
        Car.builder().roadId(startLane.getRoadId()).positionOnRoad(6).maxSpeed(16).speed(15).acceleration(0.1).build();
    carD = Car.builder()
        .roadId(oppositeLane.getRoadId())
        .positionOnRoad(oppositeLane.getLength() - 100)
        .maxSpeed(20)
        .speed(16)
        .acceleration(0.1)
        .build();
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    boolean decision = decider.overtakeDecision(carA, carEnvironment, mapFragment);
    Assertions.assertFalse(decision);
  }

  @Test
  public void getNegativeOvertakingDecisionCarBeforeOvertakingCarIsSlowingDown() {
    carA = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(0).maxSpeed(25).speed(18).acceleration(1).build();
    carB =
        Car.builder().roadId(startLane.getRoadId()).positionOnRoad(6).maxSpeed(16).speed(15).acceleration(0.1).build();
    carC = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(100).maxSpeed(20).speed(18).acceleration(-3).build();
    carD = Car.builder()
        .roadId(oppositeLane.getRoadId())
        .positionOnRoad(oppositeLane.getLength() - 400)
        .maxSpeed(20)
        .speed(16)
        .acceleration(0.1)
        .build();
    startLane.addCarAtEntry(carC);
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    boolean decision = decider.overtakeDecision(carA, carEnvironment, mapFragment);
    Assertions.assertFalse(decision);
  }

  @Test
  public void getOvertakingInformationFullScenarioTest() {
    setupCarsForGatheringInformation();
    startLane.addCarAtEntry(carC);
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carAEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carA, carAEnvironment, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(environment.isPresent()),
        () -> Assertions.assertEquals(carC.getCarId(), environment.get().getCarBeforeOvertakenCar().get().getCarId()),
        () -> Assertions.assertEquals(carD.getCarId(), environment.get().getOppositeCar().get().getCarId()),
        () -> Assertions.assertEquals(100.0, environment.get().getDistanceBeforeOvertakenCar()),
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceOnOppositeRoad()));
  }

  @Test
  public void getOvertakingInformationWithoutCCar() {
    setupCarsForGatheringInformation();
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carAEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carA, carAEnvironment, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(environment.isPresent()),
        () -> Assertions.assertTrue(environment.get().getCarBeforeOvertakenCar().isEmpty()),
        () -> Assertions.assertEquals(carD.getCarId(), environment.get().getOppositeCar().get().getCarId()),
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceBeforeOvertakenCar()),
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceOnOppositeRoad()));
  }

  @Test
  public void getOvertakingInformationWithoutDCar() {
    setupCarsForGatheringInformation();
    startLane.addCarAtEntry(carC);
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    CarEnvironment carAEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carA, carAEnvironment, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(environment.isPresent()),
        () -> Assertions.assertTrue(environment.get().getOppositeCar().isEmpty()),
        () -> Assertions.assertEquals(carC.getCarId(), environment.get().getCarBeforeOvertakenCar().get().getCarId()),
        () -> Assertions.assertEquals(100, environment.get().getDistanceBeforeOvertakenCar()),
        () -> Assertions.assertEquals(9900.0, environment.get().getDistanceOnOppositeRoad()));
  }

  @Test
  public void getOvertakingInformationWithoutCAndDCar() {
    setupCarsForGatheringInformation();
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    CarEnvironment carAEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carA, carAEnvironment, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(environment.isPresent()),
        () -> Assertions.assertTrue(environment.get().getCarBeforeOvertakenCar().isEmpty()),
        () -> Assertions.assertTrue(environment.get().getOppositeCar().isEmpty()),
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceBeforeOvertakenCar()),
        () -> Assertions.assertEquals(9900.0, environment.get().getDistanceOnOppositeRoad()));
  }

  @Test
  public void getOvertakingInformationEmpty() {
    setupCarsForGatheringInformation();
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carDEnvironment = prospector.getPrecedingCarOrCrossroad(carD, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carD, carDEnvironment, mapFragment);
    Assertions.assertTrue(environment.isEmpty());
  }

  private void setupCarsForGatheringInformation() {
    carA = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(100.0).length(4).build();
    carB = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(200.0).length(4).build();
    carC = Car.builder().roadId(startLane.getRoadId()).positionOnRoad(304.0).length(4).build();
    carD = Car.builder().roadId(oppositeLane.getRoadId()).positionOnRoad(100.0).length(4).build();
  }
}
