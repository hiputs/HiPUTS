package pl.edu.agh.hiputs.example;

import static java.lang.Thread.sleep;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.model.map.roadstructure.NeighborRoadInfo;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.utils.DeterminingNeighborhoodUtil;

public class ExampleMapFragmentProvider {

  private static final Double DEFAULT_LANE_LENGTH = 1000.0;

  public static MapFragment getSimpleMap1(MapRepository mapRepository) {
    return getSimpleMap1(true, mapRepository);
  }

  public static MapFragment getSimpleMap1(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->1)";
    return fromStringRepresentation(mapStructure, Collections.emptyMap(), withCars ? 2 : 0, mapRepository);
  }

  public static MapFragment getSimpleMap2(MapRepository mapRepository) {
    return getSimpleMap2(true, mapRepository);
  }

  public static MapFragment getSimpleMap2(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (1->4) (4->1) (4->7) (7->4)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "3400.0"}, {"2->3", "1200.0"},})
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    return fromStringRepresentation(mapStructure, roadLengths, withCars ? 2 : 0, mapRepository);
  }

  public static MapFragment getSimpleMap3(MapRepository mapRepository) {
    return getSimpleMap3(true, mapRepository);
  }

  //Map with many cars
  public static MapFragment getSimpleMap3(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (1->4) (4->1) (4->7) (7->4)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "3400.0"}, {"2->3", "1200.0"},})
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    return fromStringRepresentation(mapStructure, roadLengths, withCars ? 100 : 0, mapRepository);
  }

  public static MapFragment getSimpleMap4(MapRepository mapRepository) {
    return getSimpleMap4(true, mapRepository);
  }

  //Map with many cars
  public static MapFragment getSimpleMap4(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (1->4) (4->1) (4->7) (7->4)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "200.0"}, {"2->3", "200.0"},
            {"3->4", "200.0"}, {"4->5", "200.0"}, {"5->6", "200.0"}, {"6->7", "200.0"}, {"7->8", "200.0"},
            {"8->1", "200.0"}, {"1->4", "200.0"}, {"4->1", "200.0"}, {"4->7", "200.0"}, {"7->4", "200.0"}, })
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    return fromStringRepresentation(mapStructure, roadLengths, withCars ? 30 : 0, mapRepository);
  }

  public static MapFragment getMapWithShortRoad(MapRepository mapRepository) {
    return getMapWithShortRoad(true, mapRepository);
  }

  //Map with many cars
  public static MapFragment getMapWithShortRoad(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->1) (2->1) (3->2) (5->4) (6->5) (2->5) (5->2)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "200.0"}, {"2->3", "200.0"},
            {"3->4", "200.0"}, {"4->5", "200.0"}, {"5->6", "200.0"}, {"6->1", "200.0"}, {"2->1", "200.0"},
            {"3->2", "200.0"}, {"5->4", "200.0"}, {"6->5", "200.0"}, {"2->5", "2.0"}, {"5->2", "2.0"}, })
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    return fromStringRepresentation(mapStructure, roadLengths, withCars ? 10 : 0, mapRepository);
  }

  public static MapFragment getMapWithShortRoad2(MapRepository mapRepository) {
    return getMapWithShortRoad2(true, mapRepository);
  }

  //Map with many cars
  public static MapFragment getMapWithShortRoad2(boolean withCars, MapRepository mapRepository) {
    String mapStructure = "(1->2) (2->3) (3->4) (4->5) (5->6) (6->7) (7->8) (8->1) (7->2) (3->6)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "200.0"}, {"2->3", "2.0"},
            {"3->4", "200.0"}, {"4->5", "200.0"}, {"5->6", "200.0"}, {"6->7", "2.0"}, {"7->8", "200.0"},
            {"8->1", "200.0"}, {"7->2", "2.0"}, {"3->6", "2.0"}, })
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    return fromStringRepresentation(mapStructure, roadLengths, withCars ? 10 : 0, mapRepository);
  }

  /**
   * Simple scenario for overtaking with two lanes, without cars
   * crossable only from lane1: <br />
   * junction1 <- lane1 <- junction2 <br/>
   * ------------------------------  <br/>
   * --    ---     ---    ---    --  <br/>
   * junction1 -> lane2 -> junction2 <br/>
   * @return new MapFragment
   */
  public static MapFragment getSimpleMapForOvertaking() {
    String mapStructure = "(1->2) (2->1)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "10000"}, {"2->1", "10000"},})
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    Map<String, HorizontalSign> roadHorizontalSigns = new HashMap<>();
    roadHorizontalSigns.put("1->2", HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
    roadHorizontalSigns.put("2->1", HorizontalSign.OPPOSITE_DIRECTION_SOLID_LINE);
    Map<String, String> roadToRoadMap = Stream.of(new String[][] {{"1->2", "2->1"},})
        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
    Map<String, RoadUnderConstruction> stringRoadMap = getStringRoadMapFromStringRepresentation(mapStructure);
    Map<String, LaneUnderConstruction> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
    setOppositeRoadInformationOnRoad(stringRoadMap, roadToRoadMap, roadHorizontalSigns);

    Map<String, JunctionUnderConstruction> stringJunctionMap =
        getStringJunctionMapFromStringRepresentation(mapStructure);
    Stream.of(new String[] {"1", "2"}).forEach(v -> {
      JunctionId crossroadId = JunctionId.randomCrossroad();
      stringJunctionMap.get(v).getJunctionBuilder().junctionId(crossroadId);
      stringJunctionMap.get(v).junctionId = crossroadId;
    });

    setRoadLengths(stringRoadMap, roadLengths);

    stringRoadMap.forEach((edge, roadUnderConstruction) -> putOnMap(edge, roadUnderConstruction, stringJunctionMap));
    generateLanesOnRoad(stringRoadMap, stringLaneMap, stringJunctionMap);
    setLaneLengths(stringLaneMap, roadLengths);

    Patch patch = createPatch(stringRoadMap, stringLaneMap, stringJunctionMap);
    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random()).addLocalPatch(patch).build();
    return mapFragment;
  }

  /**
   * Simple scenario for overtaking with six lanes without cars
   * all junctions are BEND
   * crossable only from lane1, lane2, lane3, and lane4: <br/>
   * junction1 <- lane1 <- junction2 <- lane2 <- junction3 <- lane3 <- junction4 <br/>
   * ---   ---   ---   ---   ---   ---------------------------------------       <br/>
   * ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---       <br/>
   * junction1 -> lane4 -> junction2 -> lane5 -> junction3 -> lane6 -> junction4 <br/>
   * @return new MapFragment
   */
  public static MapFragment getSimpleLongerMapForOvertaking() {
    String mapStructure = "(1->2) (2->3) (3->4) (4->3) (3->2) (2->1)";
    Map<String, Double> roadLengths = Stream.of(new String[][] {{"1->2", "3000"}, {"2->1", "3000"}, {"3->4", "3000"}, {"4->3", "3000"}})
        .collect(Collectors.toMap(data -> data[0], data -> Double.parseDouble(data[1])));
    Map<String, HorizontalSign> roadHorizontalSigns = new HashMap<>();
    roadHorizontalSigns.put("1->2", HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
    roadHorizontalSigns.put("2->1", HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
    roadHorizontalSigns.put("2->3", HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
    roadHorizontalSigns.put("3->2", HorizontalSign.OPPOSITE_DIRECTION_SOLID_LINE);
    roadHorizontalSigns.put("3->4", HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
    roadHorizontalSigns.put("4->3", HorizontalSign.OPPOSITE_DIRECTION_SOLID_LINE);
    Map<String, String> roadToRoadMap = Stream.of(new String[][] {{"1->2", "2->1"}, {"2->3", "3->2"}, {"3->4", "4->3"}})
        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
    Map<String, RoadUnderConstruction> stringRoadMap = getStringRoadMapFromStringRepresentation(mapStructure);
    Map<String, LaneUnderConstruction> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
    setOppositeRoadInformationOnRoad(stringRoadMap, roadToRoadMap, roadHorizontalSigns);

    Map<String, JunctionUnderConstruction> stringJunctionMap =
        getStringJunctionMapFromStringRepresentation(mapStructure);
    Stream.of(new String[] {"2", "3"}).forEach(v -> {
      JunctionId bendId = JunctionId.randomBend();
      stringJunctionMap.get(v).getJunctionBuilder().junctionId(bendId);
      stringJunctionMap.get(v).junctionId = bendId;
    });
    Stream.of(new String[] {"1", "4"}).forEach(v -> {
      JunctionId crossroadId = JunctionId.randomCrossroad();
      stringJunctionMap.get(v).getJunctionBuilder().junctionId(crossroadId);
      stringJunctionMap.get(v).junctionId = crossroadId;
    });

    setRoadLengths(stringRoadMap, roadLengths);

    stringRoadMap.forEach((edge, roadUnderConstruction) -> putOnMap(edge, roadUnderConstruction, stringJunctionMap));
    generateLanesOnRoad(stringRoadMap, stringLaneMap, stringJunctionMap);
    setLaneLengths(stringLaneMap, roadLengths);

    Patch patch = createPatch(stringRoadMap, stringLaneMap, stringJunctionMap);
    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random()).addLocalPatch(patch).build();
    DeterminingNeighborhoodUtil.execute(List.of(patch));
    return mapFragment;
  }

  public static MapFragment fromStringRepresentation(String mapStructure, Map<String, Double> roadLengths,
      int randomCarsPerLane, MapRepository mapRepository) {
    Map<String, RoadUnderConstruction> stringRoadMap = getStringRoadMapFromStringRepresentation(mapStructure);
    Map<String, LaneUnderConstruction> stringLaneMap = getStringLaneMapFromStringRepresentation(mapStructure);
    Map<String, JunctionUnderConstruction> stringJunctionMap =
        getStringJunctionMapFromStringRepresentation(mapStructure);

    setRoadLengths(stringRoadMap, roadLengths);
    stringRoadMap.forEach((edge, roadUnderConstruction) -> putOnMap(edge, roadUnderConstruction, stringJunctionMap));
    generateLanesOnRoad(stringRoadMap, stringLaneMap, stringJunctionMap);
    setLaneLengths(stringLaneMap, roadLengths);

    Patch patch = createPatch(stringRoadMap, stringLaneMap, stringJunctionMap);

    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random()).addLocalPatch(patch).build();
    while (!mapRepository.isReady()) {
      try {
        sleep(5);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    ExampleCarProvider exampleCarProvider = new ExampleCarProvider(mapFragment, mapRepository);

    patch.streamLanesEditable().forEach(lane -> {
      for (int i = 0; i < randomCarsPerLane; i++) {
        double carPosition = (randomCarsPerLane - i) * patch.getRoadReadable(lane.getRoadId()).getLength() / (randomCarsPerLane + 1);
        Car car = exampleCarProvider.generateCar(carPosition, lane.getLaneId());
        exampleCarProvider.limitSpeedPreventCollisionOnStart(car, lane);
        lane.addCarAtEntry(car);
      }
    });

    DeterminingNeighborhoodUtil.execute(List.of(patch));

    return mapFragment;
  }

  private static Map<String, RoadUnderConstruction> getStringRoadMapFromStringRepresentation(String mapStructure) {
    return Arrays.stream(mapStructure.split(" "))
        .map(edge -> edge.substring(1, edge.length() - 1))
        .collect(Collectors.toMap(Function.identity(), e -> new RoadUnderConstruction()));
  }

  private static Map<String, LaneUnderConstruction> getStringLaneMapFromStringRepresentation(String mapStructure) {
    return Arrays.stream(mapStructure.split(" "))
        .map(edge -> edge.substring(1, edge.length() - 1))
        .collect(Collectors.toMap(Function.identity(), e -> new LaneUnderConstruction()));
  }


  private static Map<String, JunctionUnderConstruction> getStringJunctionMapFromStringRepresentation(
      String mapStructure) {
    List<String> targets = getStringRoadMapFromStringRepresentation(mapStructure).keySet().stream()
        .map(edge -> edge.split("->")[1]).collect(Collectors.toList());

    return getStringRoadMapFromStringRepresentation(mapStructure).keySet()
        .stream()
        .flatMap(edge -> Stream.of(edge.split("->")))
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.toMap(Function.identity(), v -> new JunctionUnderConstruction(getJunctionType(v, targets))));
  }

  private static void setOppositeRoadInformationOnRoad(
      Map<String, RoadUnderConstruction> stringRoadMap,
      Map<String, String> roadToRoadMap,
      Map<String, HorizontalSign> roadHorizontalSigns
  ){
    roadToRoadMap.forEach((k, v) ->{
      RoadUnderConstruction road1 = stringRoadMap.get(k);
      RoadUnderConstruction road2 = stringRoadMap.get(v);
      road1.getRoadBuilder().leftNeighbor(Optional.of(new NeighborRoadInfo(road2.getRoadId(), roadHorizontalSigns.get(k))));
      road2.getRoadBuilder().leftNeighbor(Optional.of(new NeighborRoadInfo(road1.getRoadId(), roadHorizontalSigns.get(v))));
    });

  }

  private static JunctionType getJunctionType(String junctionStringRepr, List<String> targets) {
    return targets.stream().filter(v -> v.equals(junctionStringRepr)).count() > 1 ? JunctionType.CROSSROAD : JunctionType.BEND;
  }

  private static void setRoadLengths(Map<String, RoadUnderConstruction> stringRoadMap,
      Map<String, Double> roadLengths) {
    stringRoadMap.forEach((key, roadUnderConstruction) -> roadUnderConstruction.getRoadBuilder()
        .length(Optional.ofNullable(roadLengths.get(key)).orElse(DEFAULT_LANE_LENGTH)));
  }

  private static void setLaneLengths(Map<String, LaneUnderConstruction> stringLaneMap,
      Map<String, Double> roadLengths) {
    stringLaneMap.forEach((key, roadUnderConstruction) -> roadUnderConstruction.getLaneBuilder()
        .length(Optional.ofNullable(roadLengths.get(key)).orElse(DEFAULT_LANE_LENGTH)));
  }

  private static void putOnMap(String edge, RoadUnderConstruction roadUnderConstruction,
      Map<String, JunctionUnderConstruction> stringJunctionMap) {
    String begin = edge.split("->")[0];
    String end = edge.split("->")[1];

    JunctionUnderConstruction incomingJunction = stringJunctionMap.get(begin);
    JunctionUnderConstruction outgoingJunction = stringJunctionMap.get(end);

    roadUnderConstruction.getRoadBuilder().incomingJunctionId(incomingJunction.getJunctionId());
    roadUnderConstruction.getRoadBuilder().outgoingJunctionId(outgoingJunction.getJunctionId());

    incomingJunction.getJunctionBuilder().addOutgoingRoadId(roadUnderConstruction.getRoadId());

    outgoingJunction.getJunctionBuilder().addIncomingRoadId(roadUnderConstruction.getRoadId(), false);
  }

  private static void generateLanesOnRoad(
      Map<String, RoadUnderConstruction> stringRoadMap,
      Map<String,LaneUnderConstruction> stringLaneMap,
      Map<String, JunctionUnderConstruction> stringJunctionMap) {

    stringRoadMap
        .forEach((edge, roadUnderConstruction) ->{
          String begin = edge.split("->")[0];
          String end = edge.split("->")[1];

          List<LaneId> laneSuccessors = stringLaneMap.entrySet()
              .stream()
              .filter(laneMap -> laneMap.getKey().split("->")[0].equals(end) && !laneMap.getKey().split("->")[1].equals(
                  begin))
              .map(laneMap -> laneMap.getValue().laneId)
              .toList();

          LaneUnderConstruction laneUnderConstruction = stringLaneMap.get(edge);
          roadUnderConstruction.roadBuilder
              .lanes(Collections.singletonList(laneUnderConstruction.laneId));
          laneUnderConstruction.laneBuilder
              .laneId(laneUnderConstruction.laneId)
              .roadId(roadUnderConstruction.roadId)
              .laneSuccessors(laneSuccessors);
        });

  }

  private static Patch createPatch(Map<String, RoadUnderConstruction> stringRoadMap,
      Map<String, LaneUnderConstruction> stringLaneMap,
      Map<String, JunctionUnderConstruction> stringJunctionMap) {
    return Patch.builder()
        .junctions(stringJunctionMap.values()
            .stream()
            .map(junctionUnderConstruction -> junctionUnderConstruction.getJunctionBuilder().build())
            .collect(Collectors.toMap(Junction::getJunctionId, Function.identity())))
        .roads(stringRoadMap.values()
            .stream()
            .map(roadUnderConstruction -> roadUnderConstruction.getRoadBuilder().build())
            .collect(Collectors.toMap(Road::getRoadId, Function.identity())))
        .lanes(stringLaneMap.values()
            .stream()
            .map(laneUnderConstruction -> laneUnderConstruction.getLaneBuilder().build())
            .collect(Collectors.toMap(Lane::getLaneId, Function.identity())))
        .build();
  }

  @Getter
  private static class RoadUnderConstruction {

    RoadId roadId;
    Road.RoadBuilder roadBuilder;

    public RoadUnderConstruction() {
      this.roadId = RoadId.random();
      this.roadBuilder = Road.builder().roadId(this.roadId);
    }
  }

  @Getter
  private static class LaneUnderConstruction {
    LaneId laneId;
    Lane.LaneBuilder laneBuilder;

    public LaneUnderConstruction() {
      this.laneId = LaneId.random();
      this.laneBuilder = Lane.builder().laneId(this.laneId);
    }
  }

  @Getter
  private static class JunctionUnderConstruction {

    JunctionId junctionId;
    Junction.JunctionBuilder junctionBuilder;

    public JunctionUnderConstruction(JunctionType junctionType) {
      this.junctionId = junctionType == JunctionType.CROSSROAD ?
          JunctionId.randomCrossroad() : JunctionId.randomBend();
      this.junctionBuilder = Junction.builder().junctionId(this.junctionId);
    }
  }
}
