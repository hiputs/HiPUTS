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
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;

// TODO probably most of the tests should be moved to LaneEditor tests
public class RoadStructureEditorTest {

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void addCarToLaneTest(int index) {
    //given
    TestData testData = prepareTestData();
    MapFragment mapFragment = testData.mapFragment;
    Lane lane = testData.allLanes.get(index);

    Car car = Car.builder().build();

    //when
    lane.addCarAtEntry(car);

    //then
    assertThat(lane.streamCarsFromExitReadable().count()).isEqualTo(1);
    assertThat(lane.streamCarsFromExitReadable().findFirst().get()).isEqualTo(car);
  }

  private TestData prepareTestData() {

    LaneId lane1Id = LaneId.random();
    LaneId lane2Id = LaneId.random();
    LaneId lane3Id = LaneId.random();
    LaneId lane4Id = LaneId.random();

    Lane.LaneBuilder lane1Builder = Lane.builder().laneId(lane1Id);
    Lane.LaneBuilder lane2Builder = Lane.builder().laneId(lane2Id);
    Lane.LaneBuilder lane3Builder = Lane.builder().laneId(lane3Id);
    Lane.LaneBuilder lane4Builder = Lane.builder().laneId(lane4Id);

    JunctionId junction1Id = JunctionId.randomCrossroad();
    JunctionId junction2Id = JunctionId.randomCrossroad();
    JunctionId junction3Id = JunctionId.randomCrossroad();

    Junction.JunctionBuilder junction1Builder = Junction.builder().junctionId(junction1Id);
    Junction.JunctionBuilder junction2Builder = Junction.builder().junctionId(junction2Id);
    Junction.JunctionBuilder junction3Builder = Junction.builder().junctionId(junction3Id);

    junction1Builder.addIncomingLaneId(lane1Id, false).addOutgoingLaneId(lane2Id);
    lane1Builder.outgoingJunctionId(junction1Id);
    lane2Builder.incomingJunctionId(junction1Id);

    junction2Builder.addIncomingLaneId(lane2Id, false).addOutgoingLaneId(lane3Id);
    lane2Builder.outgoingJunctionId(junction2Id);
    lane3Builder.incomingJunctionId(junction2Id);

    junction3Builder.addIncomingLaneId(lane3Id, false).addOutgoingLaneId(lane4Id);
    lane3Builder.outgoingJunctionId(junction3Id);
    lane4Builder.incomingJunctionId(junction3Id);

    Lane lane1 = lane1Builder.build();
    Lane lane2 = lane2Builder.build();
    Lane lane3 = lane3Builder.build();
    Lane lane4 = lane4Builder.build();

    Junction junction1 = junction1Builder.build();
    Junction junction2 = junction2Builder.build();
    Junction junction3 = junction3Builder.build();

    Patch localPatch = Patch.builder()
        .lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2))
        .junctions(Map.of(junction1.getJunctionId(), junction1, junction2.getJunctionId(), junction2))
        .build();

    Patch remotePatch = Patch.builder()
        .lanes(Map.of(lane3.getLaneId(), lane3, lane4.getLaneId(), lane4))
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
    Lane lane = testData.allLanes.get(index);

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
    public List<Lane> allLanes;
  }
}
