package pl.edu.agh.hiputs.model.car;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionRead;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;
import pl.edu.agh.hiputs.model.map.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.map.example.ExampleMapFragmentProvider;

import java.lang.reflect.Field;

public class GetPrecedingCarTest {
    private MapFragment mapFragment;
    private ExampleCarProvider carProvider;
    private LaneId startLaneId;
    private LaneReadWrite startLane;
    private Car car1, car2;

    @BeforeEach
    public void setup() {
        mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
        carProvider = new ExampleCarProvider(mapFragment);
        startLaneId = mapFragment.getAllManagedLaneIds().iterator().next();
        startLane = mapFragment.getLaneReadWrite(startLaneId);
        car1 = carProvider.generateCar(startLaneId, 3);
        car2 = carProvider.generateCar(startLaneId, 4);
        car1.setPosition(10.0);
        car2.setPosition(60.0);
    }

    @Test
    public void getPrecedingCarWhereCarIsFound() {
        startLane.addFirstCar(car2);
        startLane.addFirstCar(car1);
        CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
        Assertions.assertAll(
                () -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
                () -> Assertions.assertEquals(car2, carEnvironment.getPrecedingCar().get()),
                () -> Assertions.assertEquals(50.0 - car2.getLength(), carEnvironment.getDistance()),
                () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()), //in this case we get precedingCar and nextCrossroadId
                () -> Assertions.assertEquals(startLane.getOutgoingJunction(), carEnvironment.getNextCrossroadId().get())
        );
    }

    @Test
    public void getPrecedingCarWhereCarIsNotFound() {
        startLane.addFirstCar(car1);
        CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
        Assertions.assertAll(
                () -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
                () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()),
                () -> Assertions.assertEquals(startLane.getOutgoingJunction(), carEnvironment.getNextCrossroadId().get()),
                () -> Assertions.assertEquals(startLane.getLength() - car1.getPosition(), carEnvironment.getDistance())
        );
    }

    @Test
    public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBend() {
        car1 = carProvider.generateCar(startLaneId, 2);
        this.setAllJunctionTypeBend();
        car1.setPosition(10.0);
        startLane.addFirstCar(car1);
        CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
        Assertions.assertAll(
                () -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
                () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
                () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getPosition(), carEnvironment.getDistance())
        );
    }

    @Test
    public void getPrecedingCarShouldFindItself() {
        this.setAllJunctionTypeBend();
        startLane.addFirstCar(car1);
        CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
        Assertions.assertAll(
                () -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
                () -> Assertions.assertEquals(car1, carEnvironment.getPrecedingCar().get()),
                () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
                () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getLength(), carEnvironment.getDistance())
        );
    }


    @SneakyThrows
    private void setAllJunctionTypeBend() {
        for (LaneId laneId : mapFragment.getAllManagedLaneIds()) {
            JunctionRead junction = mapFragment.getJunctionReadById(mapFragment.getLaneReadWriteById(laneId).getOutgoingJunction());
            this.setJunctionTypeBend(junction);
        }
    }

    private void setJunctionTypeBend(JunctionRead junction) throws Exception {
        Field field = junction.getClass().getDeclaredField("id");
        field.setAccessible(true);
        Object junctionId = field.get(junction);
        field = junctionId.getClass().getDeclaredField("junctionType");
        field.setAccessible(true);
        field.set(junctionId, JunctionType.BEND);
    }
}
