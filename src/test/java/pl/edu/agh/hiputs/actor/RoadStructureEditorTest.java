package pl.edu.agh.hiputs.actor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.Junction;
import pl.edu.agh.hiputs.model.map.Lane;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RoadStructureEditorTest {

    private TestData prepareTestData() {
        Junction junction1 = Junction.builder().build();
        Junction junction2 = Junction.builder().build();
        Junction junction3 = Junction.builder().build();
        
        LaneId lane1Id = LaneId.random();
        LaneId lane2Id = LaneId.random();
        LaneId lane3Id = LaneId.random();
        LaneId lane4Id = LaneId.random();
        
        Lane.LaneBuilder lane1Builder = Lane.builder();
        Lane.LaneBuilder lane2Builder = Lane.builder();
        Lane.LaneBuilder lane3Builder = Lane.builder();
        Lane.LaneBuilder lane4Builder = Lane.builder();

        junction1.addIncomingLane(lane1Id, false);
        junction1.addOutgoingLane(lane2Id);
        lane1Builder.outgoingJunction(junction1.getId());
        lane2Builder.incomingJunction(junction1.getId());

        junction1.addIncomingLane(lane2Id, false);
        junction1.addOutgoingLane(lane3.getId());
        lane2.setOutgoingJunction(junction2.getId());
        lane3.setIncomingJunction(junction2.getId());

        junction1.addIncomingLane(lane3.getId(), false);
        junction1.addOutgoingLane(lane4.getId());
        lane3.setOutgoingJunction(junction3.getId());
        lane4.setIncomingJunction(junction3.getId());

        Patch localPatch = new Patch();
        localPatch.setLanes(Map.of(lane1.getId(), lane1, lane2.getId(), lane2));
        localPatch.setJunctions(Map.of(junction1.getId(), junction1, junction2.getId(), junction2));

        Patch remotePatch = new Patch();
        remotePatch.setLanes(Map.of(lane3.getId(), lane3, lane4.getId(), lane4));
        remotePatch.setJunctions(Map.of(junction3.getId(), junction3));

        MapFragment mapFragment = MapFragment.builder(MapFragmentId.random())
                .addLocalPatch(localPatch)
                .addRemotePatch(MapFragmentId.random(), remotePatch.build())
                .build();

        TestData testData = new TestData();
        testData.allLanes = List.of(lane1Builder.build(), lane2Builder.build(), lane3Builder.build(), lane4Builder.build());
        testData.mapFragment = mapFragment;
        return testData;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void addCarToLaneTest(int index) {
        //given
        TestData testData = prepareTestData();
        MapFragment mapFragment = testData.mapFragment;
        Lane lane = testData.allLanes.get(index);

        Car car = Car.builder().build();

        //when
        mapFragment.addCar(lane.getId(), car);

        //then
        assertThat(lane.getCarsQueue().size()).isEqualTo(1);
        assertThat(lane.getCarsQueue().get(0)).isEqualTo(car);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void removeCarFromLaneTest(int index) {
        //given
        TestData testData = prepareTestData();
        MapFragment mapFragment = testData.mapFragment;
        Lane lane = testData.allLanes.get(index);

        Car car1 = Car.builder().build();;
        Car car2 = Car.builder().build();;

        //when
        mapFragment.addCar(lane.getId(), car1);
        mapFragment.addCar(lane.getId(), car2);
        Optional<Car> car3 = mapFragment.removeLastCarFromLane(lane.getId());
        Optional<Car> car4 = mapFragment.removeLastCarFromLane(lane.getId());

        //then
        assertThat(lane.getCarsQueue().size()).isEqualTo(0);
        assertThat(car3.get()).isEqualTo(car1);
        assertThat(car4.get()).isEqualTo(car2);
    }

    private class TestData {
        public MapFragment mapFragment;
        public List<Lane> allLanes;
    }
}
