package pl.edu.agh.hiputs.model.driver.overtaking;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarProspector;
import pl.edu.agh.hiputs.model.car.CarProspectorImpl;
import pl.edu.agh.hiputs.model.driver.overtaking.OvertakingDecider;
import pl.edu.agh.hiputs.model.driver.overtaking.OvertakingEnvironment;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

public class OvertakingDeciderTest {

  private MapFragment mapFragment;
  private Car carA, carB, carC, carD;
  private LaneEditable startLane, oppositeLane;
  private CarProspector prospector;

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMapForOvertaking();
    startLane = mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> mapFragment.getLaneEditable(laneId))
        .filter(laneEditable -> laneEditable.getLeftNeighbor().isPresent() && laneEditable.getLeftNeighbor()
            .get()
            .getHorizontalSign()
            .equals(HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE))
        .findFirst()
        .get();
    oppositeLane = mapFragment.getLaneEditable(startLane.getLeftNeighbor().get().getLaneId());
    carA = Car.builder().laneId(startLane.getLaneId()).positionOnLane(100.0).length(4).build();
    carB = Car.builder().laneId(startLane.getLaneId()).positionOnLane(200.0).length(4).build();
    carC = Car.builder().laneId(startLane.getLaneId()).positionOnLane(304.0).length(4).build();
    carD = Car.builder().laneId(oppositeLane.getLaneId()).positionOnLane(100.0).length(4).build();
    prospector = new CarProspectorImpl();
  }

  @Test
  public void getOvertakingInformationFullScenarioTest() {
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
        () -> Assertions.assertEquals(9800.0,
            environment.get().getDistanceOnOppositeLane()));
  }

  @Test
  public void getOvertakingInformationWithoutCCar() {
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
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceOnOppositeLane())
    );
  }

  @Test
  public void getOvertakingInformationWithoutDCar() {
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
        () -> Assertions.assertEquals(9900.0, environment.get().getDistanceOnOppositeLane())
    );
  }

  @Test
  public void getOvertakingInformationWithoutCAndDCar() {
    startLane.addCarAtEntry(carB);
    startLane.addCarAtEntry(carA);
    CarEnvironment carAEnvironment = prospector.getPrecedingCarOrCrossroad(carA, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carA, carAEnvironment, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(environment.isPresent()),
        () -> Assertions.assertTrue(environment.get().getCarBeforeOvertakenCar().isEmpty()),
        () -> Assertions.assertTrue(environment.get().getOppositeCar().isEmpty()),
        () -> Assertions.assertEquals(9800.0, environment.get().getDistanceBeforeOvertakenCar()),
        () -> Assertions.assertEquals(9900.0, environment.get().getDistanceOnOppositeLane())
    );
  }

  @Test
  public void getOvertakingInformationEmpty() {
    oppositeLane.addCarAtEntry(carD);
    CarEnvironment carDEnvironment = prospector.getPrecedingCarOrCrossroad(carD, mapFragment);
    OvertakingDecider decider = new OvertakingDecider();
    Optional<OvertakingEnvironment> environment = decider.getOvertakingInformation(carD, carDEnvironment, mapFragment);
    Assertions.assertTrue(environment.isEmpty());
  }
}
