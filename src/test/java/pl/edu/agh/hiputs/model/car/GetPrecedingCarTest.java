package pl.edu.agh.hiputs.model.car;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.utils.ReflectionUtil;

public class GetPrecedingCarTest {

  private MapFragment mapFragment;
  private ExampleCarProvider carProvider;
  private LaneId startLaneId;
  private LaneEditable startLane;
  private Car car1, car2;
    @BeforeEach
    public void setup() {
        mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
        carProvider = new ExampleCarProvider(mapFragment);
        startLaneId = mapFragment.getLocalLaneIds().iterator().next();
        startLane = mapFragment.getLaneEditable(startLaneId);
        car1 = carProvider.generateCar(10.0, startLaneId, 3);
        car2 = carProvider.generateCar(60.0, startLaneId, 4);

    }


  @Test
  public void getPrecedingCarWhereCarIsFound() {
    startLane.addCarAtEntry(car2);
    startLane.addCarAtEntry(car1);
    CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertEquals(car2, carEnvironment.getPrecedingCar().get()),
        () -> Assertions.assertEquals(50.0 - car2.getLength(), carEnvironment.getDistance()),
        () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()),
        //in this case we get precedingCar and nextCrossroadId
        () -> Assertions.assertEquals(startLane.getOutgoingJunctionId(), carEnvironment.getNextCrossroadId().get()));
  }

  @Test
  public void getPrecedingCarWhereCarIsNotFound() {
    startLane.addCarAtEntry(car1);
    CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
    Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(startLane.getOutgoingJunctionId(), carEnvironment.getNextCrossroadId().get()),
        () -> Assertions.assertEquals(startLane.getLength() - car1.getPositionOnLane(), carEnvironment.getDistance()));
  }
    @Test
    public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBend() {
        car1 = carProvider.generateCar(10.0, startLaneId, 2);
        this.setAllJunctionTypeBend();

    startLane.addCarAtEntry(car1);
    CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
    Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getPositionOnLane(),
            carEnvironment.getDistance()));
  }

  @Test
  public void getPrecedingCarShouldFindItself() {
    this.setAllJunctionTypeBend();
    startLane.addCarAtEntry(car1);
    CarEnvironment carEnvironment = car1.getPrecedingCar(mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertEquals(car1, carEnvironment.getPrecedingCar().get()),
        () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getLength(), carEnvironment.getDistance()));
  }

  private void setAllJunctionTypeBend() {
    for (LaneId laneId : mapFragment.getLocalLaneIds()) {
      JunctionReadable junction =
          mapFragment.getJunctionReadable(mapFragment.getLaneEditable(laneId).getOutgoingJunctionId());
      this.setJunctionTypeBend(junction);
    }
  }

  private void setJunctionTypeBend(JunctionReadable junction) {
    Object junctionId = ReflectionUtil.getFieldValue(junction, "junctionId");
    System.out.println(junctionId);
    ReflectionUtil.setFieldValue(junctionId, "junctionType", JunctionType.BEND);
  }
}
