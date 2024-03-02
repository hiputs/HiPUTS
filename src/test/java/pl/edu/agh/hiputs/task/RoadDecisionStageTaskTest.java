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
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.tasks.RoadDecisionStageTask;
import pl.edu.agh.hiputs.service.routegenerator.CarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

@Disabled
@ExtendWith(MockitoExtension.class)
public class RoadDecisionStageTaskTest {

  private static final double DISTANCE_TO_LANE_END = 2.0;

  private Car car;
  private MapFragment mapFragment;
  private RoadId roadId;
  private LaneId laneId;
  private RoadId nextRoadId;
  private LaneId nextLaneId;

  @Mock
  private MapRepository mapRepository;
  @Mock
  private CarGeneratorService carGeneratorService;

  @BeforeEach
  public void setup() {
    mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false, mapRepository);
    roadId = mapFragment.getLocalRoadIds().stream().findAny().get();
    laneId = mapFragment.getRoadReadable(roadId).getLanes().get(0);
    car = createTestCar();
  }

  @Test
  public void laneDecisionStageTaskWithoutJumpsBetweenLanesTest() {
    //given
    setLaneIdRoadId(car, laneId, roadId);
    setPositionOnLane(car, 0);
    boolean carReplaced = false;

    mapFragment.getLaneEditable(laneId).addCarAtEntry(car);

    //when
    RoadDecisionStageTask roadDecisionStageTask =
        new RoadDecisionStageTask(mapFragment, roadId, carGeneratorService, carReplaced);
    roadDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
  }

  @Test
  public void laneDecisionStageTaskWithJumpsBetweenLanesTest() {
    //given
    RoadEditable roadEditable = mapFragment.getRoadEditable(roadId);
    LaneEditable laneEditable = mapFragment.getLaneEditable(laneId);
    boolean carReplaced = false;

    setLaneIdRoadId(car, laneId, roadId);
    setPositionOnLane(car, roadEditable.getLength() - DISTANCE_TO_LANE_END);

    laneEditable.addCarAtEntry(car);

    //when
    RoadDecisionStageTask roadDecisionStageTask =
        new RoadDecisionStageTask(mapFragment, roadId, carGeneratorService, carReplaced);
    roadDecisionStageTask.run();

    //then
    Decision decision = car.getDecision();
    Assertions.assertThat(decision).isNotNull();
    Assertions.assertThat(decision.getAcceleration()).isGreaterThan(0.0);
    Assertions.assertThat(decision.getRoadId()).isEqualTo(nextRoadId);
  }

  private Car createTestCar() {
    return Car.builder().routeWithLocation(createTestRouteWithLocation()).speed(2 * DISTANCE_TO_LANE_END).build();
  }

  private RouteWithLocation createTestRouteWithLocation() {
    RoadReadable roadReadable = mapFragment.getRoadReadable(roadId);
    nextRoadId =
        mapFragment.getJunctionReadable(roadReadable.getOutgoingJunctionId()).streamOutgoingRoadIds().findAny().get();

    return new RouteWithLocation(
        List.of(new RouteElement(null, roadId), new RouteElement(roadReadable.getOutgoingJunctionId(), nextRoadId)), 0);
  }

  private void setLaneIdRoadId(Car car, LaneId laneId, RoadId roadId) {
    ReflectionUtil.setFieldValue(car, "laneId", laneId);
    ReflectionUtil.setFieldValue(car, "roadId", roadId);
  }

  private void setPositionOnLane(Car car, double position) {
    ReflectionUtil.setFieldValue(car, "positionOnLane", position);
  }

}
