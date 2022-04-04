package pl.edu.agh.hiputs.task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.*;
import pl.edu.agh.hiputs.model.follow.IDMDecider;
import pl.edu.agh.hiputs.model.follow.IDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionRead;
import pl.edu.agh.hiputs.model.map.LaneRead;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;
import pl.edu.agh.hiputs.model.map.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;

import java.util.Arrays;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class LaneDecisionStageTaskTest {

    private static final double DISTANCE_TO_LANE_END = 2.0;

    @Mock
    private IDecider decider = new IDMDecider();

    @InjectMocks
    private Car car;
    private MapFragment mapFragment;
    private LaneId laneId;
    private LaneId nextLaneId;

    @BeforeEach
    public void setup() {
        Mockito.when(decider.makeDecision(any(), any())).thenReturn(1.0);

        mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
        laneId = mapFragment.getAllManagedLaneIds().stream().findAny().get();

        prepareTestCar();
    }

    private void prepareTestCar() {
        car.setSpeed(2 * DISTANCE_TO_LANE_END);

        LaneRead laneReadWrite = mapFragment.getLaneReadById(laneId);
        JunctionRead junctionRead = mapFragment.getJunctionReadById(laneReadWrite.getOutgoingJunction());
        Set<LaneId> junctionIds = junctionRead.getOutgoingLanesIds();
        nextLaneId = junctionIds.stream().findAny().get();
        car.setRouteLocation(new RouteLocation(new Route(Arrays.asList(new RouteElement(laneReadWrite.getOutgoingJunction(), nextLaneId)))));
    }

    @Test
    public void laneDecisionStageTaskWithoutJumpsBetweenLanesTest() {
        //given
        car.setLaneId(laneId);
        car.setPositionOnLane(0);

        mapFragment.getLaneReadWriteById(laneId).addFirstCar(car);

        //when
        LaneDecisionStageTask laneDecisionStageTask = new LaneDecisionStageTask(mapFragment, laneId);
        laneDecisionStageTask.run();

        //then
        Decision decision = getCarDecision(car);
        Assertions.assertThat(decision).isNotNull();
        Assertions.assertThat(decision.getAcceleration()).isEqualTo(1.0);
    }

    @Test
    public void laneDecisionStageTaskWithJumpsBetweenLanesTest() {
        //given
        LaneReadWrite laneReadWrite = mapFragment.getLaneReadWriteById(laneId);

        car.setLaneId(laneId);
        car.setPositionOnLane(laneReadWrite.getLength() - DISTANCE_TO_LANE_END);

        laneReadWrite.addFirstCar(car);

        //when
        LaneDecisionStageTask laneDecisionStageTask = new LaneDecisionStageTask(mapFragment, laneId);
        laneDecisionStageTask.run();

        //then
        Decision decision = getCarDecision(car);
        Assertions.assertThat(decision).isNotNull();
        Assertions.assertThat(decision.getAcceleration()).isEqualTo(1.0);
        Assertions.assertThat(decision.getLocation().getLaneId()).isEqualTo(nextLaneId);
    }

    private Decision getCarDecision(Car car) {
        return car.getDecision();
    }

}
