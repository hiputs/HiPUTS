package pl.edu.agh.hiputs.model.map.example;

import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.Junction;
import pl.edu.agh.hiputs.model.map.Lane;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleMapFragmentProvider {

    private static final Double DEFAULT_LANE_LENGTH = 1000.0;


    public static MapFragment getSimpleMap1() {
        return getSimpleMap1(true);
    }

    public static MapFragment getSimpleMap1(boolean withCars) {
        String mapStructure = "(1->2) (2->3) (3->1)";
        return fromStringRepresentation(mapStructure, Collections.emptyMap(), withCars ? 2 : 0);
    }

    public static MapFragment getSimpleMap2() {
        return getSimpleMap2(true);
    }

    public static MapFragment getSimpleMap2(boolean withCars) {
        String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (1->4) (4->1) (4->7) (7->4)";
        Map<String, Double> laneLengths = Stream.of(new String[][]{
                {"1->2", "3400.0"},
                {"2->3", "1200.0"},
        }).collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
        return fromStringRepresentation(mapStructure, laneLengths, withCars ? 2 : 0);
    }

    public static MapFragment fromStringRepresentation(String mapStructure, Map<String, Double> laneLengths, int randomCarsPerLane) {
        Map<String, Lane> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
        Map<String, Junction> stringJunctionMap = getStringJunctionMapFromStringRepresentation(mapStructure);

        setLaneLengths(stringLaneMap, laneLengths);

        stringLaneMap.forEach((key, value) -> putOnMap(key, value, stringJunctionMap));

        Patch patch = createPatch(stringLaneMap, stringJunctionMap);

        MapFragment mapFragment = MapFragment.builder()
                .addLocalPatch(patch)
                .build();

        for (int i = 0; i < randomCarsPerLane; i++) {
            for (Lane lane : stringLaneMap.values()) {
                Car car = new ExampleCarProvider(mapFragment).generateCar(lane.getId(), ThreadLocalRandom.current().nextInt(10));
                lane.addFirstCar(car);
            }
        }

        return mapFragment;
    }

    private static Map<String, Lane> getStringLaneMapFromStringRepresentation(String mapStructure) {
        return Arrays.stream(mapStructure.split(" "))
                .map(edge -> edge.substring(1, edge.length() - 1))
                .collect(Collectors.toList()).stream()
                .collect(Collectors.toMap(Function.identity(), e -> new Lane()));
    }

    private static Map<String, Junction> getStringJunctionMapFromStringRepresentation(String mapStructure) {
        return getStringLaneMapFromStringRepresentation(mapStructure).keySet().stream()
                .flatMap(edge -> Stream.of(edge.split("->")))
                .collect(Collectors.toSet()).stream()
                .collect(Collectors.toMap(Function.identity(), v -> new Junction()));
    }

    private static void setLaneLengths(Map<String, Lane> stringLaneMap, Map<String, Double> laneLengths) {
        stringLaneMap.forEach(
                (key, lane) -> lane.setLength(Optional.ofNullable(laneLengths.get(key)).orElse(DEFAULT_LANE_LENGTH)));
    }

    private static void putOnMap(String edge, Lane lane, Map<String, Junction> stringJunctionMap) {
        String begin = edge.split("->")[0];
        String end = edge.split("->")[1];

        Junction incomingJunction = stringJunctionMap.get(begin);
        Junction outgoingJunction = stringJunctionMap.get(end);

        lane.setIncomingJunction(incomingJunction.getId());
        lane.setOutgoingJunction(outgoingJunction.getId());

        incomingJunction.addOutgoingLane(lane.getId());

        outgoingJunction.addIncomingLane(lane.getId(), false);
    }

    private static Patch createPatch(Map<String, Lane> stringLaneMap, Map<String, Junction> stringJunctionMap) {
        Patch patch = new Patch();
        patch.setJunctions(stringJunctionMap.values().stream()
                .collect(Collectors.toMap(Junction::getId, Function.identity())));
        patch.setLanes(stringLaneMap.values().stream()
                .collect(Collectors.toMap(Lane::getId, Function.identity())));
        return patch;
    }
}
