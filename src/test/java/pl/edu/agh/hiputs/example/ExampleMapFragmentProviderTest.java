package pl.edu.agh.hiputs.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.car.CarReadable;


import java.util.stream.Collectors;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@Disabled
public class ExampleMapFragmentProviderTest {

    @Test
    public void checkCarOrderInSimpleMap1() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getSimpleMap1(true);
        Assertions.assertTrue(checkAllMapFragmentLanes(mapFragment));
    }

    @Test
    public void checkCarOrderInSimpleMap2() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getSimpleMap2(true);
        Assertions.assertTrue(checkAllMapFragmentLanes(mapFragment));
    }
    
    private boolean checkAllMapFragmentLanes(MapFragment mapFragment) {
        return mapFragment.getLocalRoadIds()
                .stream()
                .map(laneId -> mapFragment.getRoadReadable(laneId))
                .collect(Collectors.toList())
                .stream()
                .map(lane -> this.carsInOrderAndOnLane(lane))
                .noneMatch(b -> b==false);
    }

    private boolean carsInOrderAndOnLane(RoadReadable lane) {
        double previousCarPosition = lane.getLength();
        for (CarReadable car : lane.streamCarsFromExitReadable().collect(Collectors.toList())) {
            if (car.getPositionOnLane() > previousCarPosition || car.getPositionOnLane() <= 0)
                return false;
            previousCarPosition = car.getPositionOnLane() - car.getLength();
        }
        return true;
    }
}
