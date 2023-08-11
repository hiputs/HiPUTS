package pl.edu.agh.hiputs.example;

import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.CarReadable;


import java.util.stream.Collectors;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@Disabled
public class ExampleMapFragmentProviderTest {

    @Test
    public void checkCarOrderInSimpleMap1() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getSimpleMap1(true);
        Assertions.assertTrue(checkAllMapFragmentRoads(mapFragment));
    }

    @Test
    public void checkCarOrderInSimpleMap2() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getSimpleMap2(true);
        Assertions.assertTrue(checkAllMapFragmentRoads(mapFragment));
    }
    
    private boolean checkAllMapFragmentRoads(MapFragment mapFragment) {
        return mapFragment.getLocalRoadIds()
                .stream()
                .map(mapFragment::getRoadReadable)
                .map(RoadReadable::getLanes)
                .flatMap(Collection::stream)
                .map(mapFragment::getLaneReadable)
                .map(lane -> this.carsInOrderAndOnLane(lane))
                .noneMatch(b -> b==false);
    }

    private boolean carsInOrderAndOnLane(LaneReadable lane) {
        double previousCarPosition = lane.getLength();
        for (CarReadable car : lane.streamCarsFromExitReadable().collect(Collectors.toList())) {
            if (car.getPositionOnLane() > previousCarPosition || car.getPositionOnLane() <= 0)
                return false;
            previousCarPosition = car.getPositionOnLane() - car.getLength();
        }
        return true;
    }
}
