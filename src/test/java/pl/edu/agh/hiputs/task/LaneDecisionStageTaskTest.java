package pl.edu.agh.hiputs.task;

import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.follow.IDecider;
import pl.edu.agh.hiputs.model.follow.IdmDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

@ExtendWith(MockitoExtension.class)
public class LaneDecisionStageTaskTest {

  private static final double DISTANCE_TO_LANE_END = 2.0;

  @Mock
  private final IDecider decider = new IdmDecider();

  @InjectMocks
  private Car car;
  private MapFragment mapFragment;
  private LaneId laneId;
  private LaneId nextLaneId;

  @BeforeEach
  public void setup() {
    Mockito.when(decider.makeDecision(any(), any())).thenReturn(1.0);

    mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
    laneId = mapFragment.getLocalLaneIds().stream().findAny().get();

    prepareTestCar();
  }

  private void prepareTestCar() {
    setSpeed(car, 2 * DISTANCE_TO_LANE_END);

    LaneReadable laneReadWrite = mapFragment.getLaneReadable(laneId);
    JunctionReadable junctionRead = mapFragment.getJunctionReadable(laneReadWrite.getOutgoingJunctionId());
    nextLaneId = junctionRead.streamOutgoingLaneIds().findAny().get();

    RouteLocation routeLocation =
        new RouteLocation(Arrays.asList(new RouteElement(laneReadWrite.getOutgoingJunctionId(), nextLaneId)), 0);
    setRouteLocation(car, routeLocation);
  }

  private void setSpeed(Car car, double speed) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", speed);
  }

  private void setRouteLocation(Car car, RouteLocation routeLocation) {
    ReflectionUtil.setFieldValue(car, "routeLocation", routeLocation);
  }

  @Test
  public void laneDecisionStageTaskWithoutJumpsBetweenLanesTest() {
    //given
    setLaneId(car, laneId);
    setPositionOnLane(car, 0);

    mapFragment.getLaneEditable(laneId).addCarAtEntry(car);

    //when
    LaneDecisionStageTask laneDecisionStageTask = new LaneDecisionStageTask(mapFragment, laneId);
    laneDecisionStageTask.run();

    //then
    Decision decision = getCarDecision(car);
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isEqualTo(1.0);
  }

  private Decision getCarDecision(Car car) {
    return car.getDecision();
  }

  private void setLaneId(Car car, LaneId laneId) {
    ReflectionUtil.setFieldValue(car, "laneId", laneId);
  }

  private void setPositionOnLane(Car car, double position) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", position);
  }

  @Test
  public void laneDecisionStageTaskWithJumpsBetweenLanesTest() {
    //given
    LaneEditable laneReadWrite = mapFragment.getLaneEditable(laneId);

    setLaneId(car, laneId);
    setPositionOnLane(car, laneReadWrite.getLength() - DISTANCE_TO_LANE_END);

    laneReadWrite.addCarAtEntry(car);

    //when
    LaneDecisionStageTask laneDecisionStageTask = new LaneDecisionStageTask(mapFragment, laneId);
    laneDecisionStageTask.run();

    //then
    Decision decision = getCarDecision(car);
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isEqualTo(1.0);
    Assertions.assertThat(decision.getLaneId()).isEqualTo(nextLaneId);
  }

}
