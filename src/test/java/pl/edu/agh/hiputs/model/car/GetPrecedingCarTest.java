// package pl.edu.agh.hiputs.model.car;
//
// import java.util.HashMap;
// import java.util.Map;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import pl.edu.agh.hiputs.example.ExampleCarProvider;
// import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
// import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
// import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspectorImpl;
// import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
// import pl.edu.agh.hiputs.model.id.JunctionId;
// import pl.edu.agh.hiputs.model.id.JunctionType;
// import pl.edu.agh.hiputs.model.id.LaneId;
// import pl.edu.agh.hiputs.model.id.PatchId;
// import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
// import pl.edu.agh.hiputs.model.map.patch.Patch;
// import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
// import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
// import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
// import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
// import pl.edu.agh.hiputs.utils.ReflectionUtil;
//
// @Disabled
//
// public class GetPrecedingCarTest {
//
//   private MapFragment mapFragment;
//   @Mock
//   private MapRepository mapRepository;
//   private ExampleCarProvider carProvider;
//   private LaneId startLaneId;
//   private LaneEditable startLane;
//   private Car car1, car2;
//   private CarProspector prospector;
//
//   @BeforeEach
//   public void setup() {
//     mapFragment = ExampleMapFragmentProvider.getSimpleMap1(false, mapRepository);
//     carProvider = new ExampleCarProvider(mapFragment, mapRepository);
//     startLaneId = mapFragment.getLocalLaneIds().iterator().next();
//     startLane = mapFragment.getLaneEditable(startLaneId);
//     car1 = carProvider.generateCar(10.0, startLaneId, 3);
//     car2 = carProvider.generateCar(60.0, startLaneId, 4);
//     prospector = new CarProspectorImpl();
//   }
//
//
//   @Test
//   public void getPrecedingCarWhereCarIsFound() {
//     setAllJunctionTypeCrossroad();
//     startLane.addCarAtEntry(car2);
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertEquals(car2, carEnvironment.getPrecedingCar().get()),
//         () -> Assertions.assertEquals(50.0 - car2.getLength(), carEnvironment.getDistance()),
//         () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent())
//         //in this case we get precedingCar, nextCrossroadId is out of view range
//     );
//   }
//
//   @Test
//   public void getPrecedingCarWhereCarIsFoundInsaneViewRange() {
//     prospector = new CarProspectorImpl(10000000);
//     setAllJunctionTypeCrossroad();
//     startLane.addCarAtEntry(car2);
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertEquals(car2, carEnvironment.getPrecedingCar().get()),
//         () -> Assertions.assertEquals(50.0 - car2.getLength(), carEnvironment.getDistance()),
//         () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()),
//         //in this case we get precedingCar and nextCrossroadId
//         () -> Assertions.assertEquals(startLane.getOutgoingJunctionId(), carEnvironment.getNextCrossroadId().get()));
//   }
//   @Test
//   public void getPrecedingCarWhereCarIsNotFound() {
//     setAllJunctionTypeCrossroad();
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()));
//   }
//   @Test
//   public void getPrecedingCarWhereCarIsNotFoundInsaneViewRange() {
//     prospector = new CarProspectorImpl(10000000);
//     setAllJunctionTypeCrossroad();
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertTrue(carEnvironment.getNextCrossroadId().isPresent()),
//         () -> Assertions.assertEquals(startLane.getOutgoingJunctionId(), carEnvironment.getNextCrossroadId().get()),
//         () -> Assertions.assertEquals(startLane.getLength() - car1.getPositionOnLane(), carEnvironment.getDistance
//         ()));
//   }
//
//     @Test
//     public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBend() {
//         car1 = carProvider.generateCar(10.0, startLaneId, 2);
//         this.setAllJunctionTypeBend();
//
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()));
//   }
//
//   @Test
//   public void getPrecedingCarWhereCarIsNotFoundAndAllJunctionAreBendInsaneViewRange() {
//     prospector = new CarProspectorImpl(10000000);
//     car1 = carProvider.generateCar(10.0, startLaneId, 2);
//     this.setAllJunctionTypeBend();
//
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertFalse(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
//         () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getPositionOnLane(),
//             carEnvironment.getDistance()));
//   }
//
//   @Test
//   public void getPrecedingCarShouldFindItself() {
//     prospector = new CarProspectorImpl(10000000);
//     this.setAllJunctionTypeBend();
//     startLane.addCarAtEntry(car1);
//     CarEnvironment carEnvironment = prospector.getPrecedingCarOrCrossroad(car1, mapFragment);
//     Assertions.assertAll(() -> Assertions.assertTrue(carEnvironment.getPrecedingCar().isPresent()),
//         () -> Assertions.assertEquals(car1, carEnvironment.getPrecedingCar().get()),
//         () -> Assertions.assertFalse(carEnvironment.getNextCrossroadId().isPresent()),
//         () -> Assertions.assertEquals(3 * startLane.getLength() - car1.getLength(), carEnvironment.getDistance()));
//   }
//
//   private void setAllJunctionTypeBend() {
//     for (LaneId laneId : mapFragment.getLocalLaneIds()) {
//       JunctionReadable junction =
//           mapFragment.getJunctionReadable(mapFragment.getLaneEditable(laneId).getOutgoingJunctionId());
//       this.setJunctionTypeBend(junction);
//     }
//     recalculateJunctionsHashCodes(mapFragment);
//   }
//
//   private void setJunctionTypeBend(JunctionReadable junction) {
//     Object junctionId = ReflectionUtil.getFieldValue(junction, "junctionId");
//     ReflectionUtil.setFieldValue(junctionId, "junctionType", JunctionType.BEND);
//   }
//
//
//   private void setAllJunctionTypeCrossroad() {
//     for (LaneId laneId : mapFragment.getLocalLaneIds()) {
//       JunctionReadable junction =
//           mapFragment.getJunctionReadable(mapFragment.getLaneEditable(laneId).getOutgoingJunctionId());
//       this.setJunctionTypeCrossroad(junction);
//     }
//     recalculateJunctionsHashCodes(mapFragment);
//   }
//
//   private void setJunctionTypeCrossroad(JunctionReadable junction) {
//     Object junctionId = ReflectionUtil.getFieldValue(junction, "junctionId");
//     ReflectionUtil.setFieldValue(junctionId, "junctionType", JunctionType.CROSSROAD);
//   }
//
//   private void recalculateJunctionsHashCodes(MapFragment map){
//     Map<JunctionId, PatchId> oldJunctionIdToPatchId = (Map<JunctionId, PatchId>) ReflectionUtil.getFieldValue(map,
//     "junctionIdToPatchId");
//     Map<JunctionId, PatchId> newJunctionIdToPatchId = new HashMap<>();
//     newJunctionIdToPatchId.putAll(oldJunctionIdToPatchId);
//     ReflectionUtil.setFieldValue(map, "junctionIdToPatchId", newJunctionIdToPatchId);
//
//     Map<PatchId, Patch> knownPatches = (Map<PatchId, Patch>) ReflectionUtil.getFieldValue(map, "knownPatches");
//     knownPatches.values().forEach(p -> {
//       Map<JunctionId, Junction> oldJunctions = (Map<JunctionId, Junction>) ReflectionUtil.getFieldValue(p,
//       "junctions");
//       Map<JunctionId, Junction> newJunctions = new HashMap<>();
//       newJunctions.putAll(oldJunctions);
//       ReflectionUtil.setFieldValue(p, "junctions", newJunctions);
//     });
//   }
// }