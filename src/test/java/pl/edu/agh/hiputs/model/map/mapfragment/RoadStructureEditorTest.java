package pl.edu.agh.hiputs.model.map.mapfragment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;

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

    RoadId road1Id = RoadId.random();
    RoadId road2Id = RoadId.random();
    RoadId road3Id = RoadId.random();
    RoadId road4Id = RoadId.random();

    Lane lane1 = Lane.builder().laneId(LaneId.random()).build();
    Lane lane2 = Lane.builder().laneId(LaneId.random()).build();
    Lane lane3 = Lane.builder().laneId(LaneId.random()).build();
    Lane lane4 = Lane.builder().laneId(LaneId.random()).build();

    Road.RoadBuilder road1Builder = Road.builder().roadId(road1Id).lanes(Collections.singletonList(lane1.getLaneId()));
    Road.RoadBuilder road2Builder = Road.builder().roadId(road2Id).lanes(Collections.singletonList(lane2.getLaneId()));
    Road.RoadBuilder road3Builder = Road.builder().roadId(road3Id).lanes(Collections.singletonList(lane3.getLaneId()));
    Road.RoadBuilder road4Builder = Road.builder().roadId(road4Id).lanes(Collections.singletonList(lane4.getLaneId()));

    JunctionId junction1Id = JunctionId.randomCrossroad();
    JunctionId junction2Id = JunctionId.randomCrossroad();
    JunctionId junction3Id = JunctionId.randomCrossroad();

    Junction.JunctionBuilder junction1Builder = Junction.builder().junctionId(junction1Id);
    Junction.JunctionBuilder junction2Builder = Junction.builder().junctionId(junction2Id);
    Junction.JunctionBuilder junction3Builder = Junction.builder().junctionId(junction3Id);

    junction1Builder.addIncomingRoadId(road1Id, false).addOutgoingRoadId(road2Id);
    road1Builder.outgoingJunctionId(junction1Id);
    road2Builder.incomingJunctionId(junction1Id);

    junction2Builder.addIncomingRoadId(road2Id, false).addOutgoingRoadId(road3Id);
    road2Builder.outgoingJunctionId(junction2Id);
    road3Builder.incomingJunctionId(junction2Id);

    junction3Builder.addIncomingRoadId(road3Id, false).addOutgoingRoadId(road4Id);
    road3Builder.outgoingJunctionId(junction3Id);
    road4Builder.incomingJunctionId(junction3Id);

    Road road1 = road1Builder.build();
    Road road2 = road2Builder.build();
    Road road3 = road3Builder.build();
    Road road4 = road4Builder.build();

    Junction junction1 = junction1Builder.build();
    Junction junction2 = junction2Builder.build();
    Junction junction3 = junction3Builder.build();

    Patch localPatch = Patch.builder()
        .roads(Map.of(road1.getRoadId(), road1, road2.getRoadId(), road2))
        .junctions(Map.of(junction1.getJunctionId(), junction1, junction2.getJunctionId(), junction2))
        .lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2))
        .build();

    Patch remotePatch = Patch.builder()
        .roads(Map.of(road3.getRoadId(), road3, road4.getRoadId(), road4))
        .lanes(Map.of(lane3.getLaneId(), lane3, lane4.getLaneId(), lane4))
        .junctions(Map.of(junction3.getJunctionId(), junction3))
        .lanes(Map.of())
        .build();

    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random())
        .addLocalPatch(localPatch)
        .addRemotePatch(MapFragmentId.random(), remotePatch)
        .build();

    TestData testData = new TestData();
    testData.allRoads = List.of(road1Builder.build(), road2Builder.build(), road3Builder.build(), road4Builder.build());
    testData.allLanes = List.of(lane1, lane2, lane3, lane4);
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
    public List<Road> allRoads;
    public List<Lane> allLanes;
  }
}
