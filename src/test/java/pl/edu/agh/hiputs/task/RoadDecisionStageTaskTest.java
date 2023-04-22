package pl.edu.agh.hiputs.task;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.tasks.RoadDecisionStageTask;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

@Disabled
@ExtendWith(MockitoExtension.class)
public class RoadDecisionStageTaskTest {

  private static final double DISTANCE_TO_LANE_END = 2.0;

  private Car car;
  private MapFragment mapFragment;
  private RoadId laneId;
  private RoadId nextLaneId;

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
    laneId = mapFragment.getLocalRoadIds().stream().findAny().get();
    car = createTestCar();
  }

  @Test
  public void laneDecisionStageTaskWithoutJumpsBetweenLanesTest() {
    //given
    setLaneId(car, laneId);
    setPositionOnLane(car, 0);

    mapFragment.getRoadEditable(laneId).addCarAtEntry(car);

    //when
    RoadDecisionStageTask roadDecisionStageTask = new RoadDecisionStageTask(mapFragment, laneId);
    roadDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
  }

  @Test
  public void laneDecisionStageTaskWithJumpsBetweenLanesTest() {
    //given
    RoadEditable laneReadWrite = mapFragment.getRoadEditable(laneId);

    setLaneId(car, laneId);
    setPositionOnLane(car, laneReadWrite.getLength() - DISTANCE_TO_LANE_END);

    laneReadWrite.addCarAtEntry(car);

    //when
    RoadDecisionStageTask roadDecisionStageTask = new RoadDecisionStageTask(mapFragment, laneId);
    roadDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
    Assertions.assertThat(decision.getRoadId()).isEqualTo(nextLaneId);
  }

  private Car createTestCar() {
    return Car.builder().routeWithLocation(createTestRouteWithLocation()).speed(2 * DISTANCE_TO_LANE_END).build();
  }

  private RouteWithLocation createTestRouteWithLocation() {
    RoadReadable laneReadWrite = mapFragment.getRoadReadable(laneId);
    nextLaneId =
        mapFragment.getJunctionReadable(laneReadWrite.getOutgoingJunctionId()).streamOutgoingRoadIds().findAny().get();

    return new RouteWithLocation(
        List.of(new RouteElement(null, laneId), new RouteElement(laneReadWrite.getOutgoingJunctionId(), nextLaneId)), 0);
  }

  private void setLaneId(Car car, RoadId laneId) {
    ReflectionUtil.setFieldValue(car, "laneId", laneId);
  }

  private void setPositionOnLane(Car car, double position) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", position);
  }

}
