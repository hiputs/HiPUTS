package pl.edu.agh.hiputs.model.map.mapfragment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;

// TODO probably most of the tests should be moved to LaneEditor tests
public class RoadStructureEditorTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void addCarToLaneTest(int index) {
    //given
    TestData testData = prepareTestData();
    MapFragment mapFragment = testData.mapFragment;
    Road road = testData.allLanes.get(index);

    Car car = Car.builder().build();

    //when
    road.addCarAtEntry(car);

    //then
    assertThat(road.streamCarsFromExitReadable().count()).isEqualTo(1);
    assertThat(road.streamCarsFromExitReadable().findFirst().get()).isEqualTo(car);
  }

  private TestData prepareTestData() {

    RoadId lane1Id = RoadId.random();
    RoadId lane2Id = RoadId.random();
    RoadId lane3Id = RoadId.random();
    RoadId lane4Id = RoadId.random();

    Road.RoadBuilder lane1Builder = Road.builder().roadId(lane1Id);
    Road.RoadBuilder lane2Builder = Road.builder().roadId(lane2Id);
    Road.RoadBuilder lane3Builder = Road.builder().roadId(lane3Id);
    Road.RoadBuilder lane4Builder = Road.builder().roadId(lane4Id);

    JunctionId junction1Id = JunctionId.randomCrossroad();
    JunctionId junction2Id = JunctionId.randomCrossroad();
    JunctionId junction3Id = JunctionId.randomCrossroad();

    Junction.JunctionBuilder junction1Builder = Junction.builder().junctionId(junction1Id);
    Junction.JunctionBuilder junction2Builder = Junction.builder().junctionId(junction2Id);
    Junction.JunctionBuilder junction3Builder = Junction.builder().junctionId(junction3Id);

    junction1Builder.addIncomingRoadId(lane1Id, false).addOutgoingRoadId(lane2Id);
    lane1Builder.outgoingJunctionId(junction1Id);
    lane2Builder.incomingJunctionId(junction1Id);

    junction2Builder.addIncomingRoadId(lane2Id, false).addOutgoingRoadId(lane3Id);
    lane2Builder.outgoingJunctionId(junction2Id);
    lane3Builder.incomingJunctionId(junction2Id);

    junction3Builder.addIncomingRoadId(lane3Id, false).addOutgoingRoadId(lane4Id);
    lane3Builder.outgoingJunctionId(junction3Id);
    lane4Builder.incomingJunctionId(junction3Id);

    Road lane1 = lane1Builder.build();
    Road lane2 = lane2Builder.build();
    Road lane3 = lane3Builder.build();
    Road lane4 = lane4Builder.build();

    Junction junction1 = junction1Builder.build();
    Junction junction2 = junction2Builder.build();
    Junction junction3 = junction3Builder.build();

    Patch localPatch = Patch.builder()
        .roads(Map.of(lane1.getRoadId(), lane1, lane2.getRoadId(), lane2))
        .junctions(Map.of(junction1.getJunctionId(), junction1, junction2.getJunctionId(), junction2))
        .build();

    Patch remotePatch = Patch.builder()
        .roads(Map.of(lane3.getRoadId(), lane3, lane4.getRoadId(), lane4))
        .junctions(Map.of(junction3.getJunctionId(), junction3))
        .build();

    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random())
        .addLocalPatch(localPatch)
        .addRemotePatch(MapFragmentId.random(), remotePatch)
        .build();

    TestData testData = new TestData();
    testData.allLanes = List.of(lane1Builder.build(), lane2Builder.build(), lane3Builder.build(), lane4Builder.build());
    testData.mapFragment = mapFragment;
    return testData;
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void removeCarFromLaneTest(int index) {
    //given
    TestData testData = prepareTestData();
    MapFragment mapFragment = testData.mapFragment;
    Road lane = testData.allLanes.get(index);

    Car car1 = Car.builder().build();
    Car car2 = Car.builder().build();

    //when
    lane.addCarAtEntry(car1);
    lane.addCarAtEntry(car2);
    Optional<CarEditable> car3 = lane.pollCarAtExit();
    Optional<CarEditable> car4 = lane.pollCarAtExit();

    //then
    assertThat(lane.streamCarsFromExitReadable().count()).isEqualTo(0);
    assertThat(car3.get()).isEqualTo(car1);
    assertThat(car4.get()).isEqualTo(car2);
  }

  private class TestData {

    public MapFragment mapFragment;
    public List<Road> allLanes;
  }
}
