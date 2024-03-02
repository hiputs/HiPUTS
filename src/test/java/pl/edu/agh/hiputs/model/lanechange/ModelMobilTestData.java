package pl.edu.agh.hiputs.model.lanechange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction.JunctionBuilder;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.model.map.roadstructure.Road.RoadBuilder;

public class ModelMobilTestData {

  public MapFragment mapFragment;
  public List<Road> allRoads;
  public List<Lane> allLanes;

  public List<RouteElement> routeElements1;
  public List<RouteElement> routeElements2;

  public LaneId getLaneId(int index) {
    int laneIdx = index % 10;
    int roadIdx = index / 10 - 1;
    return allLanes.get(2 * roadIdx + laneIdx).getLaneId();
  }

  public static ModelMobilTestData prepareTestData(double roadLength) {
    /*
     *          PATCH1                   \           PATCH2
     *      road1              road2              road3            road4
     * *-----lane10---> * -----lane20---> * -----lane30---> * -----lane40--->*
     *  -----lane11--->   -----lane21--->   -----lane31--->   -----lane41--->
     * 0 Bend           1 Bend            2 Bend             3 Crossroad     4 Bend
     *                                                      \\
     *                                                       \\  road5
     *                                                 lane51 \\ lane50
     *                                                         v
     *                                                          5 Bend
     *
     * lane31 - successors [lane41, lane50, lane51]
     * */
    RoadId road1Id = RoadId.random();
    RoadId road2Id = RoadId.random();
    RoadId road3Id = RoadId.random();
    RoadId road4Id = RoadId.random();
    RoadId road5Id = RoadId.random();

    LaneId laneId10 = LaneId.random();
    LaneId laneId20 = LaneId.random();
    LaneId laneId30 = LaneId.random();
    LaneId laneId40 = LaneId.random();
    LaneId laneId50 = LaneId.random();

    LaneId laneId11 = LaneId.random();
    LaneId laneId21 = LaneId.random();
    LaneId laneId31 = LaneId.random();
    LaneId laneId41 = LaneId.random();
    LaneId laneId51 = LaneId.random();

    Lane lane10 =
        Lane.builder().laneId(laneId10).roadId(road1Id).length(roadLength).laneSuccessors(List.of(laneId20)).build();
    Lane lane20 =
        Lane.builder().laneId(laneId20).roadId(road2Id).length(roadLength).laneSuccessors(List.of(laneId30)).build();
    Lane lane30 =
        Lane.builder().laneId(laneId30).roadId(road3Id).length(roadLength).laneSuccessors(List.of(laneId40)).build();
    Lane lane40 = Lane.builder().laneId(laneId40).roadId(road4Id).length(roadLength).build();
    Lane lane50 = Lane.builder().laneId(laneId50).roadId(road5Id).length(roadLength).build();

    Lane lane11 =
        Lane.builder().laneId(laneId11).roadId(road1Id).length(roadLength).laneSuccessors(List.of(laneId21)).build();
    Lane lane21 =
        Lane.builder().laneId(laneId21).roadId(road2Id).length(roadLength).laneSuccessors(List.of(laneId31)).build();
    Lane lane31 = Lane.builder()
        .laneId(laneId31)
        .roadId(road3Id)
        .length(roadLength)
        .laneSuccessors(List.of(laneId41, laneId50, laneId51))
        .build();
    Lane lane41 = Lane.builder().laneId(laneId41).roadId(road4Id).length(roadLength).build();
    Lane lane51 = Lane.builder().laneId(laneId51).roadId(road5Id).length(roadLength).build();

    RoadBuilder road1Builder = Road.builder().roadId(road1Id).lanes(List.of(laneId10, laneId11)).length(roadLength);
    RoadBuilder road2Builder = Road.builder().roadId(road2Id).lanes(List.of(laneId20, laneId21)).length(roadLength);
    RoadBuilder road3Builder = Road.builder().roadId(road3Id).lanes(List.of(laneId30, laneId31)).length(roadLength);
    RoadBuilder road4Builder = Road.builder().roadId(road4Id).lanes(List.of(laneId40, laneId41)).length(roadLength);
    RoadBuilder road5Builder = Road.builder().roadId(road5Id).lanes(List.of(laneId50, laneId51)).length(roadLength);

    JunctionId junction0Id = JunctionId.randomBend();
    JunctionId junction1Id = JunctionId.randomBend();
    JunctionId junction2Id = JunctionId.randomBend();
    JunctionId junction3Id = JunctionId.randomCrossroad();
    JunctionId junction4Id = JunctionId.randomBend();
    JunctionId junction5Id = JunctionId.randomBend();

    JunctionBuilder junction0Builder = Junction.builder().junctionId(junction0Id);
    JunctionBuilder junction1Builder = Junction.builder().junctionId(junction1Id);
    JunctionBuilder junction2Builder = Junction.builder().junctionId(junction2Id);
    JunctionBuilder junction3Builder = Junction.builder().junctionId(junction3Id);
    JunctionBuilder junction4Builder = Junction.builder().junctionId(junction4Id);
    JunctionBuilder junction5Builder = Junction.builder().junctionId(junction4Id);

    junction0Builder.addOutgoingRoadId(road1Id);
    road1Builder.incomingJunctionId(junction1Id);

    junction1Builder.addIncomingRoadId(road1Id, false).addOutgoingRoadId(road2Id);
    road1Builder.outgoingJunctionId(junction1Id);
    road2Builder.incomingJunctionId(junction1Id);

    junction2Builder.addIncomingRoadId(road2Id, false).addOutgoingRoadId(road3Id);
    road2Builder.outgoingJunctionId(junction2Id);
    road3Builder.incomingJunctionId(junction2Id);

    junction3Builder.addIncomingRoadId(road3Id, false).addOutgoingRoadId(road4Id).addOutgoingRoadId(road5Id);
    road3Builder.outgoingJunctionId(junction3Id);
    road4Builder.incomingJunctionId(junction3Id);
    road5Builder.incomingJunctionId(junction3Id);

    junction4Builder.addIncomingRoadId(road4Id, false);
    road4Builder.outgoingJunctionId(junction4Id);

    junction5Builder.addIncomingRoadId(road5Id, false);
    road5Builder.outgoingJunctionId(junction5Id);

    Road road1 = road1Builder.build();
    Road road2 = road2Builder.build();
    Road road3 = road3Builder.build();
    Road road4 = road4Builder.build();
    Road road5 = road5Builder.build();

    Junction junction0 = junction0Builder.build();
    Junction junction1 = junction1Builder.build();
    Junction junction2 = junction2Builder.build();
    Junction junction3 = junction3Builder.build();
    Junction junction4 = junction4Builder.build();
    Junction junction5 = junction5Builder.build();

    Patch localPatch = Patch.builder()
        .roads(Map.of(road1.getRoadId(), road1, road2.getRoadId(), road2))
        .junctions(Map.of(junction0.getJunctionId(), junction0, junction1.getJunctionId(), junction1,
            junction2.getJunctionId(), junction2))
        .lanes(Map.of(laneId10, lane10, laneId11, lane11, laneId20, lane20, laneId21, lane21))
        .build();

    Patch remotePatch = Patch.builder()
        .roads(Map.of(road3.getRoadId(), road3, road4.getRoadId(), road4, road5Id, road5))
        .junctions(Map.of(junction3Id, junction3, junction4Id, junction4, junction5Id, junction5))
        .lanes(
            Map.of(laneId30, lane30, laneId31, lane31, laneId40, lane40, laneId41, lane41, laneId50, lane50, laneId51,
                lane51))
        .build();

    MapFragment mapFragment = MapFragment.builder(MapFragmentId.random())
        .addLocalPatch(localPatch)
        .addRemotePatch(MapFragmentId.random(), remotePatch)
        .build();

    List<RouteElement> routeElements1 = new ArrayList<>();
    routeElements1.add(new RouteElement(junction0Id, road1Id));
    routeElements1.add(new RouteElement(junction1Id, road2Id));
    routeElements1.add(new RouteElement(junction2Id, road3Id));
    routeElements1.add(new RouteElement(junction3Id, road4Id));

    List<RouteElement> routeElements2 = new ArrayList<>();
    routeElements2.add(new RouteElement(junction0Id, road1Id));
    routeElements2.add(new RouteElement(junction1Id, road2Id));
    routeElements2.add(new RouteElement(junction2Id, road3Id));
    routeElements2.add(new RouteElement(junction3Id, road5Id));

    ModelMobilTestData modelMobilTestData = new ModelMobilTestData();
    modelMobilTestData.allRoads =
        List.of(road1Builder.build(), road2Builder.build(), road3Builder.build(), road4Builder.build());
    modelMobilTestData.allLanes =
        List.of(lane10, lane11, lane20, lane21, lane30, lane31, lane40, lane41, lane50, lane51);
    modelMobilTestData.mapFragment = mapFragment;
    modelMobilTestData.routeElements1 = routeElements1;
    modelMobilTestData.routeElements2 = routeElements2;
    return modelMobilTestData;
  }

}
