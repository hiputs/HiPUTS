package pl.edu.agh.hiputs.task;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.CarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

@Disabled
@ExtendWith(MockitoExtension.class)
public class LaneDecisionStageTaskTest {

  private static final double DISTANCE_TO_LANE_END = 2.0;

  private Car car;
  private MapFragment mapFragment;
  private LaneId laneId;
  private LaneId nextLaneId;

  @Mock
  private MapRepository mapRepository;
  @Mock
  private CarGeneratorService carGeneratorService;

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false, mapRepository);
    laneId = mapFragment.getLocalLaneIds().stream().findAny().get();
    car = createTestCar();
  }

  @Test
  public void laneDecisionStageTaskWithoutJumpsBetweenLanesTest() {
    //given
    setLaneId(car, laneId);
    setPositionOnLane(car, 0);
    boolean carReplaced = false;

    mapFragment.getLaneEditable(laneId).addCarAtEntry(car);

    //when
    LaneDecisionStageTask laneDecisionStageTask =
        new LaneDecisionStageTask(mapFragment, laneId, carGeneratorService, carReplaced);
    laneDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
  }

  @Test
  public void laneDecisionStageTaskWithJumpsBetweenLanesTest() {
    //given
    LaneEditable laneReadWrite = mapFragment.getLaneEditable(laneId);
    boolean carReplaced = false;

    setLaneId(car, laneId);
    setPositionOnLane(car, laneReadWrite.getLength() - DISTANCE_TO_LANE_END);

    laneReadWrite.addCarAtEntry(car);

    //when
    LaneDecisionStageTask laneDecisionStageTask =
        new LaneDecisionStageTask(mapFragment, laneId, carGeneratorService, carReplaced);
    laneDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
    Assertions.assertThat(decision.getLaneId()).isEqualTo(nextLaneId);
  }

  private Car createTestCar() {
    return Car.builder().routeWithLocation(createTestRouteWithLocation()).speed(2 * DISTANCE_TO_LANE_END).build();
  }

  private RouteWithLocation createTestRouteWithLocation() {
    LaneReadable laneReadWrite = mapFragment.getLaneReadable(laneId);
    nextLaneId =
        mapFragment.getJunctionReadable(laneReadWrite.getOutgoingJunctionId()).streamOutgoingLaneIds().findAny().get();

    return new RouteWithLocation(
        List.of(new RouteElement(null, laneId), new RouteElement(laneReadWrite.getOutgoingJunctionId(), nextLaneId)), 0);
  }

  private void setLaneId(Car car, LaneId laneId) {
    ReflectionUtil.setFieldValue(car, "laneId", laneId);
  }

  private void setPositionOnLane(Car car, double position) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", position);
  }

}
