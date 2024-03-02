package pl.edu.agh.hiputs.model.car;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarEnvironmentProvider;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.utils.ReflectionUtil;
@Disabled

public class GetPrecedingCarTest {

  private MapFragment mapFragment;
  private ExampleCarProvider carProvider;
  private RoadId startRoadId;
  private RoadEditable startRoad;
  private LaneId startLaneId;
  private LaneEditable startLane;
  private Car car1, car2;
  private CarProspector prospector;
    @BeforeEach
    public void setup() {
        mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false);
        carProvider = new ExampleCarProvider(mapFragment);
      startRoadId = mapFragment.getLocalRoadIds().iterator().next();
      startRoad = mapFragment.getRoadEditable(startRoadId);
      startLaneId = startRoad.getLanes().get(0);
      startLane = mapFragment.getLaneEditable(startLaneId);
        car1 = carProvider.generateCar(10.0, startLaneId, 3);
        car2 = carProvider.generateCar(60.0, startLaneId, 4);
      prospector = new CarEnvironmentProvider();
    }


  @Test
  public void getPrecedingCarWhereCarIsFound() {
    setAllJunctionTypeCrossroad();
    startLane.addCarAtEntry(car2);
    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertEquals(car2, carPrecedingEnvironment.getPrecedingCar().get()),
        () -> Assertions.assertEquals(50.0 - car2.getLength(), carPrecedingEnvironment.getDistance()),
        () -> Assertions.assertFalse(carPrecedingEnvironment.getNextCrossroadId().isPresent())
        //in this case we get precedingCar, nextCrossroadId is out of view range
    );
  }

  @Test
  public void getPrecedingCarWhereCarIsFoundInsaneViewRange() {
    prospector = new CarEnvironmentProvider(10000000);
    setAllJunctionTypeCrossroad();
    startLane.addCarAtEntry(car2);
    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertEquals(car2, carPrecedingEnvironment.getPrecedingCar().get()),
        () -> Assertions.assertEquals(50.0 - car2.getLength(), carPrecedingEnvironment.getDistance()),
        () -> Assertions.assertTrue(carPrecedingEnvironment.getNextCrossroadId().isPresent()),
        //in this case we get precedingCar and nextCrossroadId
        () -> Assertions.assertEquals(startRoad.getOutgoingJunctionId(),
            carPrecedingEnvironment.getNextCrossroadId().get()));
  }
  @Test
  public void getPrecedingCarWhereCarIsNotFound() {
    setAllJunctionTypeCrossroad();
    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertFalse(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertFalse(carPrecedingEnvironment.getNextCrossroadId().isPresent()));
  }
  @Test
  public void getPrecedingCarWhereCarIsNotFoundInsaneViewRange() {
    prospector = new CarEnvironmentProvider(10000000);
    setAllJunctionTypeCrossroad();
    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertFalse(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertTrue(carPrecedingEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(startRoad.getOutgoingJunctionId(),
            carPrecedingEnvironment.getNextCrossroadId().get()),
        () -> Assertions.assertEquals(startRoad.getLength() - car1.getPositionOnLane(),
            carPrecedingEnvironment.getDistance()));
  }

    @Test
    public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBend() {
        car1 = carProvider.generateCar(10.0, startLaneId, 2);
        this.setAllJunctionTypeBend();

    startLane.addCarAtEntry(car1);
      CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
      Assertions.assertAll(() -> Assertions.assertFalse(carPrecedingEnvironment.getPrecedingCar().isPresent()),
          () -> Assertions.assertFalse(carPrecedingEnvironment.getNextCrossroadId().isPresent()));
  }

  @Test
  public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBendInsaneViewRange() {
    prospector = new CarEnvironmentProvider(10000000);
    car1 = carProvider.generateCar(10.0, startLaneId, 2);
    this.setAllJunctionTypeBend();

    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertFalse(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertFalse(carPrecedingEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(3 * startRoad.getLength() - car1.getPositionOnLane(),
            carPrecedingEnvironment.getDistance()));
  }

  @Test
  public void getPrecedingCarShouldFindItself() {
    prospector = new CarEnvironmentProvider(10000000);
    this.setAllJunctionTypeBend();
    startLane.addCarAtEntry(car1);
    CarPrecedingEnvironment carPrecedingEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
    Assertions.assertAll(() -> Assertions.assertTrue(carPrecedingEnvironment.getPrecedingCar().isPresent()),
        () -> Assertions.assertEquals(car1, carPrecedingEnvironment.getPrecedingCar().get()),
        () -> Assertions.assertFalse(carPrecedingEnvironment.getNextCrossroadId().isPresent()),
        () -> Assertions.assertEquals(3 * startRoad.getLength() - car1.getLength(),
            carPrecedingEnvironment.getDistance()));
  }

  private void setAllJunctionTypeBend() {
    for (RoadId laneId : mapFragment.getLocalRoadIds()) {
      JunctionReadable junction =
          mapFragment.getJunctionReadable(mapFragment.getRoadEditable(laneId).getOutgoingJunctionId());
      this.setJunctionTypeBend(junction);
    }
    recalculateJunctionsHashCodes(mapFragment);
  }

  private void setJunctionTypeBend(JunctionReadable junction) {
    Object junctionId = ReflectionUtil.getFieldValue(junction, "junctionId");
    ReflectionUtil.setFieldValue(junctionId, "junctionType", JunctionType.BEND);
  }


  private void setAllJunctionTypeCrossroad() {
    for (RoadId laneId : mapFragment.getLocalRoadIds()) {
      JunctionReadable junction =
          mapFragment.getJunctionReadable(mapFragment.getRoadEditable(laneId).getOutgoingJunctionId());
      this.setJunctionTypeCrossroad(junction);
    }
    recalculateJunctionsHashCodes(mapFragment);
  }

  private void setJunctionTypeCrossroad(JunctionReadable junction) {
    Object junctionId = ReflectionUtil.getFieldValue(junction, "junctionId");
    ReflectionUtil.setFieldValue(junctionId, "junctionType", JunctionType.CROSSROAD);
  }

  private void recalculateJunctionsHashCodes(MapFragment map){
    Map<JunctionId, PatchId> oldJunctionIdToPatchId = (Map<JunctionId, PatchId>) ReflectionUtil.getFieldValue(map, "junctionIdToPatchId");
    Map<JunctionId, PatchId> newJunctionIdToPatchId = new HashMap<>();
    newJunctionIdToPatchId.putAll(oldJunctionIdToPatchId);
    ReflectionUtil.setFieldValue(map, "junctionIdToPatchId", newJunctionIdToPatchId);

    Map<PatchId, Patch> knownPatches = (Map<PatchId, Patch>) ReflectionUtil.getFieldValue(map, "knownPatches");
    knownPatches.values().forEach(p -> {
      Map<JunctionId, Junction> oldJunctions = (Map<JunctionId, Junction>) ReflectionUtil.getFieldValue(p, "junctions");
      Map<JunctionId, Junction> newJunctions = new HashMap<>();
      newJunctions.putAll(oldJunctions);
      ReflectionUtil.setFieldValue(p, "junctions", newJunctions);
    });
  }
}