package pl.edu.agh.hiputs.actor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.Junction;
import pl.edu.agh.hiputs.model.map.Lane;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RoadStructureEditorTest {

    private TestData prepareTestData() {
        Lane lane1 = new Lane();
        Lane lane2 = new Lane();
        Lane lane3 = new Lane();
        Lane lane4 = new Lane();

        Junction junction1 = new Junction();
        junction1.addIncomingLane(lane1.getId(), false);
        junction1.addOutgoingLane(lane2.getId());
        lane1.setOutgoingJunction(junction1.getId());
        lane2.setIncomingJunction(junction1.getId());

        Junction junction2 = new Junction();
        junction1.addIncomingLane(lane2.getId(), false);
        junction1.addOutgoingLane(lane3.getId());
        lane2.setOutgoingJunction(junction2.getId());
        lane3.setIncomingJunction(junction2.getId());

        Junction junction3 = new Junction();
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

        MapFragment mapFragment = MapFragment.builder()
                .addLocalPatch(localPatch)
                .addRemotePatch(remotePatch)
                .build();

        TestData testData = new TestData();
        testData.allLanes = List.of(lane1, lane2, lane3, lane4);
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
