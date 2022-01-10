package pl.edu.agh.model.map.example;

import pl.edu.agh.model.actor.ActorContext;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.Patch;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleActorContextProvider {

    public static ActorContext getSimpleMap1() {
        String mapStructure = "(1->2) (2->3) (3->1)";
        return fromStringRepresentation(mapStructure);
    }

    public static ActorContext getSimpleMap2() {
        String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (1->4) (4->1) (4->7) (7->4)";
        return fromStringRepresentation(mapStructure);
    }

    public static ActorContext fromStringRepresentation(String mapStructure) {
        Map<String, Lane> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
        Map<String, Junction> stringJunctionMap = getStringJunctionMapFromStringRepresentation(mapStructure);

        stringLaneMap.forEach((key, value) -> putOnMap(key, value, stringJunctionMap));

        Patch patch = createPatch(stringLaneMap, stringJunctionMap);

        return ActorContext.builder()
                .addLocalPatch(patch)
                .build();
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

    private static void putOnMap(String edge, Lane lane, Map<String, Junction> stringJunctionMap) {
        String begin = edge.split("->")[0];
        String end = edge.split("->")[1];

        Junction incomingJunction = stringJunctionMap.get(begin);
        Junction outgoingJunction = stringJunctionMap.get(end);

        lane.setIncomingJunction(incomingJunction.getId());
        lane.setOutgoingJunction(outgoingJunction.getId());

        Set<LaneId> incomingJunctionOutgoingLanes = incomingJunction.getOutgoingLanes();
        incomingJunctionOutgoingLanes.add(lane.getId());
        incomingJunction.setIncomingLanes(incomingJunctionOutgoingLanes);

        Set<LaneId> outgoingJunctionIncomingLanes = outgoingJunction.getIncomingLanes();
        outgoingJunctionIncomingLanes.add(lane.getId());
        outgoingJunction.setIncomingLanes(outgoingJunctionIncomingLanes);
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
