package pl.edu.agh.hiputs.task;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;
import pl.edu.agh.hiputs.model.map.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.tasks.LaneUpdateStageTask;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public class LaneUpdateStageTaskTest {
    private MapFragment mapFragment;
    private LaneId laneId1, laneId2;
    private LaneReadWrite lane1, lane2;
    private RouteLocation routeLocation = Mockito.mock(RouteLocation.class);
    private Car car1 = Car.builder().length(4).speed(15).routeLocation(routeLocation).build();
    private Car car2 = Car.builder().length(4).speed(15).routeLocation(routeLocation).build();
    private Car car3 = Car.builder().length(4).speed(15).routeLocation(routeLocation).build();
    private Decision decision1, decision2, decision3;

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
                .laneId(laneId2)
                .positionOnLane(10.0)
                .offsetToMoveOnRoute(1)
                .build();

        decision2 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .laneId(laneId2)
                .positionOnLane(20.0)
                .offsetToMoveOnRoute(1)
                .build();

        decision3 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .laneId(laneId2)
                .positionOnLane(30.0)
                .offsetToMoveOnRoute(0)
                .build();
    }

    private void setDecision(Car car, String fieldName, Decision decision) throws Exception {
        Field field = car.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(car, decision);
    }

    private void setLaneId(Car car, String fieldName, LaneId laneId) throws Exception {
        Field field = car.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(car, laneId);
    }

    private void setPositionOnLane(Car car, String fieldName, Double position) throws Exception {
        Field field = car.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(car, position);
    }

    @SneakyThrows
    @Test
    public void laneUpdateStageTaskWithoutIncomingCars() {
        setLaneId(car1, "laneId", laneId2);
        setLaneId(car2, "laneId", laneId2);
        setLaneId(car3, "laneId", laneId2);
        setPositionOnLane(car1, "positionOnLane", 0.0);
        setPositionOnLane(car2, "positionOnLane", 5.0);
        setPositionOnLane(car3, "positionOnLane", 10.0);
        lane2.addFirstCar(car3);
        lane2.addFirstCar(car2);
        lane2.addFirstCar(car1);
        setDecision(car1, "decision", decision1);
        setDecision(car2, "decision", decision2);
        setDecision(car3, "decision", decision3);

        LaneUpdateStageTask laneUpdateStageTask = new LaneUpdateStageTask(mapFragment, laneId2);

        laneUpdateStageTask.run();
        Assertions.assertAll(
                () -> Assertions.assertEquals(car1.getLaneId(), decision1.getLaneId()),
                () -> Assertions.assertEquals(car1.getPositionOnLane(), decision1.getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLaneId(), decision2.getLaneId()),
                () -> Assertions.assertEquals(car2.getPositionOnLane(), decision2.getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLaneId(), decision3.getLaneId()),
                () -> Assertions.assertEquals(car3.getPositionOnLane(), decision3.getPositionOnLane()),
                () -> Assertions.assertEquals(lane2.getCars().size(), 3),
                () -> Assertions.assertEquals(lane2.getCars().get(0), car1),
                () -> Assertions.assertEquals(lane2.getCars().get(1), car2),
                () -> Assertions.assertEquals(lane2.getCars().get(2), car3)
        );
    }

    @SneakyThrows
    @Test
    public void laneUpdateStageTaskWithIncomingCars() {
        setLaneId(car1, "laneId", laneId1);
        setLaneId(car2, "laneId", laneId1);
        setLaneId(car3, "laneId", laneId2);
        setPositionOnLane(car1, "positionOnLane", 987.0);
        setPositionOnLane(car2, "positionOnLane", 997.0);
        setPositionOnLane(car3, "positionOnLane", 10.0);
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
                () -> Assertions.assertEquals(car1.getLaneId(), decision1.getLaneId()),
                () -> Assertions.assertEquals(car1.getPositionOnLane(), decision1.getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLaneId(), decision2.getLaneId()),
                () -> Assertions.assertEquals(car2.getPositionOnLane(), decision2.getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLaneId(), decision3.getLaneId()),
                () -> Assertions.assertEquals(car3.getPositionOnLane(), decision3.getPositionOnLane()),
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
        setLaneId(car1, "laneId", laneId1);
        setLaneId(car2, "laneId", laneId1);
        setLaneId(car3, "laneId", laneId2);
        setPositionOnLane(car1, "positionOnLane", 800.0);
        setPositionOnLane(car2, "positionOnLane", 997.0);
        setPositionOnLane(car3, "positionOnLane", 10.0);

        lane1.addFirstCar(car2);
        lane1.addFirstCar(car1);
        lane2.addFirstCar(car3);
        decision1 = Decision.builder()
                .acceleration(2.0)
                .speed(12.0)
                .laneId(laneId1)
                .positionOnLane(900.0)
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
                () -> Assertions.assertEquals(car1.getLaneId(), decision1.getLaneId()),
                () -> Assertions.assertEquals(car1.getPositionOnLane(), decision1.getPositionOnLane()),
                () -> Assertions.assertEquals(car2.getLaneId(), decision2.getLaneId()),
                () -> Assertions.assertEquals(car2.getPositionOnLane(), decision2.getPositionOnLane()),
                () -> Assertions.assertEquals(car3.getLaneId(), decision3.getLaneId()),
                () -> Assertions.assertEquals(car3.getPositionOnLane(), decision3.getPositionOnLane()),
                () -> Assertions.assertEquals(lane2.getCars().size(), 2),
                () -> Assertions.assertEquals(lane1.getCars().size(), 1),
                () -> Assertions.assertEquals(lane2.getIncomingCars().size(), 0),
                () -> Assertions.assertEquals(lane1.getCars().get(0), car1),
                () -> Assertions.assertEquals(lane2.getCars().get(0), car2),
                () -> Assertions.assertEquals(lane2.getCars().get(1), car3)
        );
    }
}
