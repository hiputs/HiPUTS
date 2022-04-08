package pl.edu.agh.hiputs.model.map.example;

import lombok.Getter;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.Junction;
import pl.edu.agh.hiputs.model.map.Lane;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.*;
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
        Map<String, LaneUnderConstruction> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
        Map<String, JunctionUnderConstruction> stringJunctionMap = getStringJunctionMapFromStringRepresentation(mapStructure);

        setLaneLengths(stringLaneMap, laneLengths);
        stringLaneMap.forEach((edge, laneUnderConstruction) -> putOnMap(edge, laneUnderConstruction, stringJunctionMap));

        Patch patch = createPatch(stringLaneMap, stringJunctionMap);

        MapFragment mapFragment = MapFragment.builder(MapFragmentId.random()).addLocalPatch(patch).build();
        ExampleCarProvider exampleCarProvider = new ExampleCarProvider(mapFragment);

        patch.streamLanesEditable().forEach(lane -> {
            for (int i = 0; i < randomCarsPerLane; i++) {
                Car car = exampleCarProvider.generateCar(lane.getId(), ThreadLocalRandom.current().nextInt(10));
                lane.addCarAtEntry(car);
            }
        });
        
        return mapFragment;
    }

    private static Map<String, LaneUnderConstruction> getStringLaneMapFromStringRepresentation(String mapStructure) {
        return Arrays.stream(mapStructure.split(" "))
                .map(edge -> edge.substring(1, edge.length() - 1))
                .collect(Collectors.toMap(Function.identity(), e -> new LaneUnderConstruction()));
    }

    private static Map<String, JunctionUnderConstruction> getStringJunctionMapFromStringRepresentation(String mapStructure) {
        return getStringLaneMapFromStringRepresentation(mapStructure).keySet().stream()
                .flatMap(edge -> Stream.of(edge.split("->")))
                .collect(Collectors.toSet()).stream()
                .collect(Collectors.toMap(Function.identity(), v -> new JunctionUnderConstruction()));
    }

    private static void setLaneLengths(Map<String, LaneUnderConstruction> stringLaneMap,
                                       Map<String, Double> laneLengths) {
        stringLaneMap.forEach((key, laneUnderConstruction) ->
                laneUnderConstruction.getLaneBuilder()
                        .length(Optional.ofNullable(laneLengths.get(key)).orElse(DEFAULT_LANE_LENGTH))
        );
    }

    private static void putOnMap(String edge, LaneUnderConstruction laneUnderConstruction,
                                 Map<String, JunctionUnderConstruction> stringJunctionMap) {
        String begin = edge.split("->")[0];
        String end = edge.split("->")[1];

        JunctionUnderConstruction incomingJunction = stringJunctionMap.get(begin);
        JunctionUnderConstruction outgoingJunction = stringJunctionMap.get(end);
    
        laneUnderConstruction.getLaneBuilder().incomingJunction(incomingJunction.getJunctionId());
        laneUnderConstruction.getLaneBuilder().outgoingJunction(outgoingJunction.getJunctionId());

        incomingJunction.getJunctionBuilder().addOutgoingLane(laneUnderConstruction.getLaneId());

        outgoingJunction.getJunctionBuilder().addIncomingLane(laneUnderConstruction.getLaneId(), false);
    }

    private static Patch createPatch(Map<String, LaneUnderConstruction> stringLaneMap,
                                     Map<String, JunctionUnderConstruction> stringJunctionMap) {
        return Patch.builder()
                .junctions(stringJunctionMap.values().stream()
                        .map(junctionUnderConstruction -> junctionUnderConstruction.getJunctionBuilder().build())
                        .collect(Collectors.toMap(Junction::getId, Function.identity())))
                .lanes(stringLaneMap.values().stream()
                        .map(laneUnderConstruction -> laneUnderConstruction.getLaneBuilder().build())
                        .collect(Collectors.toMap(Lane::getId, Function.identity())))
                .build();
    }
    
    @Getter
    private static class LaneUnderConstruction {
        LaneId laneId;
        Lane.LaneBuilder laneBuilder;
    
        public LaneUnderConstruction() {
            this.laneId = LaneId.random();
            this.laneBuilder = Lane.builder().id(this.laneId);
        }
    }
    
    @Getter
    private static class JunctionUnderConstruction {
        JunctionId junctionId;
        Junction.JunctionBuilder junctionBuilder;
        
        public JunctionUnderConstruction() {
            this.junctionId = JunctionId.randomCrossroad();
            this.junctionBuilder = Junction.builder().id(this.junctionId);
        }
    }
}
