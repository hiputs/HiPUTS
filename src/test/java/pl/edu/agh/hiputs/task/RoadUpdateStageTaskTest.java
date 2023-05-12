package pl.edu.agh.hiputs.task;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.tasks.RoadUpdateStageTask;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

@Disabled
@ExtendWith(MockitoExtension.class)
public class RoadUpdateStageTaskTest {

  private MapFragment mapFragment;
  private RoadId laneId1, laneId2;
  private RoadEditable lane1, lane2;
  private final RouteWithLocation routeWithLocation = Mockito.mock(RouteWithLocation.class);
  private final Car car1 = Car.builder().length(4).speed(15).routeWithLocation(routeWithLocation).build();
  private final Car car2 = Car.builder().length(4).speed(15).routeWithLocation(routeWithLocation).build();
  private final Car car3 = Car.builder().length(4).speed(15).routeWithLocation(routeWithLocation).build();
  private Decision decision1, decision2, decision3;

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
    laneId1 = mapFragment.getLocalRoadIds().iterator().next();
    lane1 = mapFragment.getRoadEditable(laneId1);
    laneId2 = mapFragment.getJunctionReadable(lane1.getOutgoingJunctionId()).streamOutgoingRoadIds().findFirst().get();
    lane2 = mapFragment.getRoadEditable(laneId2);
    decision1 = Decision.builder()
        .acceleration(2.0)
        .speed(12.0)
        .roadId(laneId2)
        .positionOnRoad(10.0)
        .offsetToMoveOnRoute(1)
        .build();

    decision2 = Decision.builder()
        .acceleration(2.0)
        .speed(12.0)
        .roadId(laneId2)
        .positionOnRoad(20.0)
        .offsetToMoveOnRoute(1)
        .build();

    decision3 = Decision.builder()
        .acceleration(2.0)
        .speed(12.0)
        .roadId(laneId2)
        .positionOnRoad(30.0)
        .offsetToMoveOnRoute(0)
        .build();

    when(routeWithLocation.moveForward(anyInt())).thenReturn(true);
  }

  private void setDecision(Car car, Decision decision) {
    ReflectionUtil.setFieldValue(car, "decision", decision);
  }

  private void setLaneId(Car car, RoadId laneId) {
    ReflectionUtil.setFieldValue(car, "laneId", laneId);
  }

  private void setPositionOnLane(Car car, double position) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", position);
  }

  @Test
  public void laneUpdateStageTaskWithoutIncomingCars() {
    setLaneId(car1, laneId2);
    setLaneId(car2, laneId2);
    setLaneId(car3, laneId2);
    setPositionOnLane(car1, 0.0);
    setPositionOnLane(car2, 5.0);
    setPositionOnLane(car3, 10.0);
    lane2.addCarAtEntry(car3);
    lane2.addCarAtEntry(car2);
    lane2.addCarAtEntry(car1);
    setDecision(car1, decision1);
    setDecision(car2, decision2);
    setDecision(car3, decision3);

    RoadUpdateStageTask roadUpdateStageTask = new RoadUpdateStageTask(mapFragment, laneId2);

    roadUpdateStageTask.run();
    Assertions.assertAll(() -> Assertions.assertEquals(decision1.getRoadId(), car1.getRoadId()),
        () -> Assertions.assertEquals(decision1.getPositionOnRoad(), car1.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision2.getRoadId(), car2.getRoadId()),
        () -> Assertions.assertEquals(decision2.getPositionOnRoad(), car2.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision3.getRoadId(), car3.getRoadId()),
        () -> Assertions.assertEquals(decision3.getPositionOnRoad(), car3.getPositionOnRoad()),
        () -> Assertions.assertEquals(3, lane2.streamCarsFromExitEditable().count()),
        () -> Assertions.assertEquals(car3, lane2.streamCarsFromExitEditable().findFirst().get()),
        () -> Assertions.assertEquals(car2, lane2.streamCarsFromExitEditable().skip(1).findFirst().get()),
        () -> Assertions.assertEquals(car1, lane2.streamCarsFromExitEditable().skip(2).findFirst().get()));
  }

  @Test
  public void laneUpdateStageTaskWithIncomingCars() {
    setLaneId(car1, laneId1);
    setLaneId(car2, laneId1);
    setLaneId(car3, laneId2);
    setPositionOnLane(car1, 987.0);
    setPositionOnLane(car2, 997.0);
    setPositionOnLane(car3, 10.0);
    lane1.addCarAtEntry(car2);
    lane1.addCarAtEntry(car1);
    lane2.addCarAtEntry(car3);
    setDecision(car1, decision1);
    setDecision(car2, decision2);
    setDecision(car3, decision3);

    lane2.addIncomingCar(car1);
    lane2.addIncomingCar(car2);

    RoadUpdateStageTask roadUpdateStageTask1 = new RoadUpdateStageTask(mapFragment, laneId1);
    roadUpdateStageTask1.run();
    RoadUpdateStageTask roadUpdateStageTask2 = new RoadUpdateStageTask(mapFragment, laneId2);
    roadUpdateStageTask2.run();

    Assertions.assertAll(() -> Assertions.assertEquals(decision1.getRoadId(), car1.getRoadId()),
        () -> Assertions.assertEquals(decision1.getPositionOnRoad(), car1.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision2.getRoadId(), car2.getRoadId()),
        () -> Assertions.assertEquals(decision2.getPositionOnRoad(), car2.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision3.getRoadId(), car3.getRoadId()),
        () -> Assertions.assertEquals(decision3.getPositionOnRoad(), car3.getPositionOnRoad()),
        () -> Assertions.assertEquals(3, lane2.streamCarsFromExitEditable().count()),
        () -> Assertions.assertEquals(0, lane1.streamCarsFromExitEditable().count()),
        () -> Assertions.assertEquals(0, lane2.pollIncomingCars().count()),
        () -> Assertions.assertEquals(car3, lane2.streamCarsFromExitEditable().findFirst().get()),
        () -> Assertions.assertEquals(car2, lane2.streamCarsFromExitEditable().skip(1).findFirst().get()),
        () -> Assertions.assertEquals(car1, lane2.streamCarsFromExitEditable().skip(2).findFirst().get()));
  }

  @Test
  public void laneUpdateStageTaskWithOneIncomingCar() {
    setLaneId(car1, laneId1);
    setLaneId(car2, laneId1);
    setLaneId(car3, laneId2);
    setPositionOnLane(car1, 800.0);
    setPositionOnLane(car2, 997.0);
    setPositionOnLane(car3, 10.0);

    lane1.addCarAtEntry(car2);
    lane1.addCarAtEntry(car1);
    lane2.addCarAtEntry(car3);
    decision1 = Decision.builder()
        .acceleration(2.0)
        .speed(12.0)
        .roadId(laneId1)
        .positionOnRoad(900.0)
        .offsetToMoveOnRoute(0)
        .build();
    setDecision(car1, decision1);
    setDecision(car2, decision2);
    setDecision(car3, decision3);

    lane2.addIncomingCar(car2);

    RoadUpdateStageTask roadUpdateStageTask1 = new RoadUpdateStageTask(mapFragment, laneId1);
    roadUpdateStageTask1.run();
    RoadUpdateStageTask roadUpdateStageTask2 = new RoadUpdateStageTask(mapFragment, laneId2);
    roadUpdateStageTask2.run();

    Assertions.assertAll(() -> Assertions.assertEquals(decision1.getRoadId(), car1.getRoadId()),
        () -> Assertions.assertEquals(decision1.getPositionOnRoad(), car1.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision2.getRoadId(), car2.getRoadId()),
        () -> Assertions.assertEquals(decision2.getPositionOnRoad(), car2.getPositionOnRoad()),
        () -> Assertions.assertEquals(decision3.getRoadId(), car3.getRoadId()),
        () -> Assertions.assertEquals(decision3.getPositionOnRoad(), car3.getPositionOnRoad()),
        () -> Assertions.assertEquals(2, lane2.streamCarsFromExitEditable().count()),
        () -> Assertions.assertEquals(1, lane1.streamCarsFromExitEditable().count()),
        () -> Assertions.assertEquals(0, lane2.pollIncomingCars().count()),
        () -> Assertions.assertEquals(car1, lane1.streamCarsFromExitEditable().findFirst().get()),
        () -> Assertions.assertEquals(car3, lane2.streamCarsFromExitEditable().findFirst().get()),
        () -> Assertions.assertEquals(car2, lane2.streamCarsFromExitEditable().skip(1).findFirst().get()));
  }
}
