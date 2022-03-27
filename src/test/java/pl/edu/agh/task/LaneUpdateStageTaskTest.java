package pl.edu.agh.task;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.car.*;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.LaneReadWrite;
import pl.edu.agh.model.map.example.ExampleMapFragmentProvider;
import pl.edu.agh.tasks.LaneUpdateStageTask;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public class LaneUpdateStageTaskTest {
    private MapFragment mapFragment;
    private LaneId laneId1, laneId2;
    private LaneReadWrite lane1, lane2;
    private RouteLocation routeLocation = Mockito.mock(RouteLocation.class);
    private Car car1 = new Car(4, 15, routeLocation);
    private Car car2 = new Car(4, 15, routeLocation);
    private Car car3 = new Car(4, 15, routeLocation);
    private Decision decision1, decision2, decision3;
    private LaneLocation location1, location2, location3;

    @BeforeEach
    public void setup() {
        mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
        laneId1 = mapFragment.getAllManagedLaneIds().iterator().next();
        lane1 = mapFragment.getLaneReadWriteById(laneId1);
        laneId2 = mapFragment.getJunctionReadById(lane1.getOutgoingJunction()).getOutgoingLanesIds().iterator().next();
        lane2 = mapFragment.getLaneReadWriteById(laneId2);
        decision1 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .location(new LaneLocation(laneId2, 10.0))
                .offsetToMoveOnRoute(1)
                .build();

        decision2 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .location(new LaneLocation(laneId2, 20.0))
                .offsetToMoveOnRoute(1)
                .build();

        decision3 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .location(new LaneLocation(laneId2, 30.0))
                .offsetToMoveOnRoute(0)
                .build();
    }

    private void setDecision(Car car, String fieldName, Decision decision) throws Exception {
        Field field = car.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(car, decision);
    }

    @SneakyThrows
    @Test
    public void laneUpdateStageTaskWithoutIncomingCars() {
        location1 = new LaneLocation(laneId2, 0.0);
        location2 = new LaneLocation(laneId2, 5.0);
        location3 = new LaneLocation(laneId2, 10.0);
        car1.setLocation(location1);
        car2.setLocation(location2);
        car3.setLocation(location3);
        lane2.addFirstCar(car3);
        lane2.addFirstCar(car2);
        lane2.addFirstCar(car1);
        setDecision(car1, "decision", decision1);
        setDecision(car2, "decision", decision2);
        setDecision(car3, "decision", decision3);

        LaneUpdateStageTask laneUpdateStageTask = new LaneUpdateStageTask(mapFragment, laneId2);

        laneUpdateStageTask.run();
        Assertions.assertAll(
                () -> Assertions.assertEquals(car1.getLocation().getLane(), decision1.getLocation().getLane()),
                () -> Assertions.assertEquals(car1.getPosition(), decision1.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLocation().getLane(), decision2.getLocation().getLane()),
                () -> Assertions.assertEquals(car2.getPosition(), decision2.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLocation().getLane(), decision3.getLocation().getLane()),
                () -> Assertions.assertEquals(car3.getPosition(), decision3.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(lane2.getCars().size(), 3),
                () -> Assertions.assertEquals(lane2.getCars().get(0), car1),
                () -> Assertions.assertEquals(lane2.getCars().get(1), car2),
                () -> Assertions.assertEquals(lane2.getCars().get(2), car3)
        );
    }

    @SneakyThrows
    @Test
    public void laneUpdateStageTaskWithIncomingCars() {
        location1 = new LaneLocation(laneId1, 987.0);
        location2 = new LaneLocation(laneId1, 997.0);
        location3 = new LaneLocation(laneId2, 10.0);
        car1.setLocation(location1);
        car2.setLocation(location2);
        car3.setLocation(location3);
        lane1.addFirstCar(car2);
        lane1.addFirstCar(car1);
        lane2.addFirstCar(car3);
        setDecision(car1, "decision", decision1);
        setDecision(car2, "decision", decision2);
        setDecision(car3, "decision", decision3);

        lane2.addToIncomingCars(car1);
        lane2.addToIncomingCars(car2);

        LaneUpdateStageTask laneUpdateStageTask1 = new LaneUpdateStageTask(mapFragment, laneId1);
        laneUpdateStageTask1.run();
        LaneUpdateStageTask laneUpdateStageTask2 = new LaneUpdateStageTask(mapFragment, laneId2);
        laneUpdateStageTask2.run();

        Assertions.assertAll(
                () -> Assertions.assertEquals(car1.getLocation().getLane(), decision1.getLocation().getLane()),
                () -> Assertions.assertEquals(car1.getPosition(), decision1.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLocation().getLane(), decision2.getLocation().getLane()),
                () -> Assertions.assertEquals(car2.getPosition(), decision2.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLocation().getLane(), decision3.getLocation().getLane()),
                () -> Assertions.assertEquals(car3.getPosition(), decision3.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(lane2.getCars().size(), 3),
                () -> Assertions.assertEquals(lane1.getCars().size(), 0),
                () -> Assertions.assertEquals(lane2.getIncomingCars().size(), 0),
                () -> Assertions.assertEquals(lane2.getCars().get(0), car1),
                () -> Assertions.assertEquals(lane2.getCars().get(1), car2),
                () -> Assertions.assertEquals(lane2.getCars().get(2), car3)
        );
    }

    @SneakyThrows
    @Test
    public void laneUpdateStageTaskWithOneIncomingCar() {
        location1 = new LaneLocation(laneId1, 800.0);
        location2 = new LaneLocation(laneId1, 997.0);
        location3 = new LaneLocation(laneId2, 10.0);
        car1.setLocation(location1);
        car2.setLocation(location2);
        car3.setLocation(location3);
        lane1.addFirstCar(car2);
        lane1.addFirstCar(car1);
        lane2.addFirstCar(car3);
        decision1 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .location(new LaneLocation(laneId1, 900.0))
                .offsetToMoveOnRoute(0)
                .build();
        setDecision(car1, "decision", decision1);
        setDecision(car2, "decision", decision2);
        setDecision(car3, "decision", decision3);

        lane2.addToIncomingCars(car2);

        LaneUpdateStageTask laneUpdateStageTask1 = new LaneUpdateStageTask(mapFragment, laneId1);
        laneUpdateStageTask1.run();
        LaneUpdateStageTask laneUpdateStageTask2 = new LaneUpdateStageTask(mapFragment, laneId2);
        laneUpdateStageTask2.run();

        Assertions.assertAll(
                () -> Assertions.assertEquals(car1.getLocation().getLane(), decision1.getLocation().getLane()),
                () -> Assertions.assertEquals(car1.getPosition(), decision1.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLocation().getLane(), decision2.getLocation().getLane()),
                () -> Assertions.assertEquals(car2.getPosition(), decision2.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLocation().getLane(), decision3.getLocation().getLane()),
                () -> Assertions.assertEquals(car3.getPosition(), decision3.getLocation().getPositionOnLane()),
                () -> Assertions.assertEquals(lane2.getCars().size(), 2),
                () -> Assertions.assertEquals(lane1.getCars().size(), 1),
                () -> Assertions.assertEquals(lane2.getIncomingCars().size(), 0),
                () -> Assertions.assertEquals(lane1.getCars().get(0), car1),
                () -> Assertions.assertEquals(lane2.getCars().get(0), car2),
                () -> Assertions.assertEquals(lane2.getCars().get(1), car3)
        );
    }
}
