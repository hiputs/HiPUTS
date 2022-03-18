package pl.edu.agh.task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.Decision;
import pl.edu.agh.model.car.LaneLocation;
import pl.edu.agh.model.car.Route;
import pl.edu.agh.model.car.RouteElement;
import pl.edu.agh.model.car.RouteLocation;
import pl.edu.agh.model.follow.IDMDecider;
import pl.edu.agh.model.follow.IDecider;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.JunctionRead;
import pl.edu.agh.model.map.LaneRead;
import pl.edu.agh.model.map.LaneReadWrite;
import pl.edu.agh.model.map.example.ExampleMapFragmentProvider;
import pl.edu.agh.tasks.LaneDecisionStageTask;

import java.lang.reflect.Field;
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
        LaneLocation laneLocation = new LaneLocation();
        laneLocation.setLane(laneId);
        laneLocation.setPositionOnLane(0);
        car.setLocation(laneLocation);

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

        LaneLocation laneLocation = new LaneLocation();
        laneLocation.setLane(laneId);
        laneLocation.setPositionOnLane(laneReadWrite.getLength() - DISTANCE_TO_LANE_END);
        car.setLocation(laneLocation);

        laneReadWrite.addFirstCar(car);

        //when
        LaneDecisionStageTask laneDecisionStageTask = new LaneDecisionStageTask(mapFragment, laneId);
        laneDecisionStageTask.run();

        //then
        Decision decision = getCarDecision(car);
        Assertions.assertThat(decision).isNotNull();
        Assertions.assertThat(decision.getAcceleration()).isEqualTo(1.0);
        Assertions.assertThat(decision.getLocation().getLane()).isEqualTo(nextLaneId);
    }

    private Decision getCarDecision(Car car) {
        try {
            Field decisionField = car.getClass().getDeclaredField("decision");
            decisionField.setAccessible(true);
            return (Decision) decisionField.get(car);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
