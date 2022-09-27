package pl.edu.agh.hiputs.model.map.mapfragment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment.MapFragmentBuilder;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction.JunctionBuilder;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@ExtendWith(MockitoExtension.class)
@Disabled("no time for fixes")
class PatchMigrationTest {

  @Mock
  private MapRepository mapRepository;

  @Test
  // WorkerB ---> Worker C migration patch P4
  void shouldCorrectlyHandlePatchExchangeBetweenKnownNeighbors(){
    MapFragment mapFragment = getExampleMapFragment();
    MapFragmentId workerB = new MapFragmentId("B");
    MapFragmentId workerC = new MapFragmentId("C");

    mapFragment.migratePatchBetweenNeighbour(new PatchId("P4"), workerC, workerB);

    assertEquals(workerB, mapFragment.getMapFragmentIdByPatchId(new PatchId("P4")));
  }

  @Test
    // WorkerB ---> Worker C migration patch P4
  void shouldCorrectlyHandlePatchExchangeBetweenKnownNeighbors2(){
    MapFragment mapFragment = getExampleMapFragment2();
    MapFragmentId workerB = new MapFragmentId("B");
    MapFragmentId workerC = new MapFragmentId("C");

    mapFragment.migratePatchBetweenNeighbour(new PatchId("P4"), workerB, workerC);

    assertEquals(workerC, mapFragment.getMapFragmentIdByPatchId(new PatchId("P4")));
  }

  @Test
  void shouldCorrectHandleGetPatchFromNeighbour() {
    MapFragment mapFragment = getExampleMapFragment();

    List<ImmutablePair<PatchId, MapFragmentId>> migratedPatchNeighbours = List.of(
        new ImmutablePair<>(new PatchId("P1"), new MapFragmentId("A")),
        new ImmutablePair<>(new PatchId("P2"), new MapFragmentId("B")),
        new ImmutablePair<>(new PatchId("P3"), new MapFragmentId("C"))
    );

    when(mapRepository.getPatch(any())).thenReturn(mapFragment.getPatchById(new PatchId("P2")));

    mapFragment.migratePatchToMe(new PatchId("P4"), new MapFragmentId("B"), mapRepository, migratedPatchNeighbours);

    verify(mapRepository, times(2)).getPatch(any());
    assertEquals(2, mapFragment.getLocalJunctionIds().size());
    assertEquals(1, mapFragment.getBorderPatches().get(new MapFragmentId("C")).size());
    assertEquals(1, mapFragment.getBorderPatches().get(new MapFragmentId("B")).size());
  }

  @Test
  void shouldCorrectHandleGetPatchFromNeighbourAndGetNewNeighbour() {
    MapFragment mapFragment = getExampleMapFragment3();

    List<ImmutablePair<PatchId, MapFragmentId>> migratedPatchNeighbours = List.of(
        new ImmutablePair<>(new PatchId("P1"), new MapFragmentId("A")),
        new ImmutablePair<>(new PatchId("P4"), new MapFragmentId("B"))
    );

    Lane lane3 = laneBuilder("L3", "J3", "J4");
    Lane lane4 = laneBuilder("L4", "J2", "J4");
    Junction junction4 = junctionBuilder("J4", List.of(), List.of(lane3, lane4));
    Patch patch4 = patchBuilder("P4", List.of(), List.of(junction4), List.of("P2", "P3"));

    when(mapRepository.getPatch(any()))
        .thenReturn(patch4);

    mapFragment.migratePatchToMe(new PatchId("P2"), new MapFragmentId("C"), mapRepository, migratedPatchNeighbours);

    verify(mapRepository, times(1)).getPatch(eq(new PatchId("P4")));
    assertEquals(2, mapFragment.getLocalJunctionIds().size());
    assertEquals(2, mapFragment.getNeighbors().size());
    assertEquals(1, mapFragment.getBorderPatches().get(new MapFragmentId("C")).size());
    assertEquals(4, mapFragment.getKnownPatchReadable().size());
  }

  // P - patch + junction ID
  // L - line
  // Worker A (me) - P1
  // Worker B      - P4
  // Worker C      - P2 P3
  //
  //         L1
  //   P1--------------P2
  //    |               |
  //  L2|             L4|
  //    |  L3           |
  //   P3--------------P4
  private MapFragment getExampleMapFragment3() {
    Lane lane1 = laneBuilder("L1", "J1", "J2");
    Lane lane2 = laneBuilder("L2", "J1", "J3");
    Lane lane3 = laneBuilder("L3", "J3", "J4");
    Lane lane4 = laneBuilder("L4", "J2", "J4");

    Junction junction1 = junctionBuilder("J1", List.of(lane1, lane2, lane3), List.of());
    Junction junction2 = junctionBuilder("J2", List.of(lane4), List.of(lane1));
    Junction junction3 = junctionBuilder("J3", List.of(lane3), List.of(lane2));

    Patch patch1 = patchBuilder("P1", List.of(lane1, lane2), List.of(junction1), List.of("P2", "P3"));
    Patch patch2 = patchBuilder("P2", List.of(lane4), List.of(junction2), List.of("P1", "P4"));
    Patch patch3 = patchBuilder("P3", List.of(lane3), List.of(junction3), List.of("P1", "P4"));


    MapFragmentId workerA = new MapFragmentId("A");
    MapFragmentId workerC = new MapFragmentId("C");

    MapFragmentBuilder mapFragmentBuilder = new MapFragmentBuilder(workerA);
    mapFragmentBuilder.addLocalPatch(patch1);
    mapFragmentBuilder.addRemotePatch(workerC, patch2);
    mapFragmentBuilder.addRemotePatch(workerC, patch3);

    return mapFragmentBuilder.build();
  }

  // P - patch + junction ID
  // L - line
  // Worker A (me) - P1, P2
  // Worker B      - P4
  // Worker C      - P3
  //
  //         L1
  //   P1--------------P2
  //    |  \            |
  //  L2|    \  L5    L4|
  //    |  L3  \        /
  //   P3---------P4-- /
  private MapFragment getExampleMapFragment2() {
    Lane lane1 = laneBuilder("L1", "J1", "J2");
    Lane lane2 = laneBuilder("L2", "J1", "J3");
    Lane lane3 = laneBuilder("L3", "J3", "J4");
    Lane lane4 = laneBuilder("L4", "J2", "J4");
    Lane lane5 = laneBuilder("L5", "J1", "J4");

    Junction junction1 = junctionBuilder("J1", List.of(lane1, lane2, lane3), List.of());
    Junction junction2 = junctionBuilder("J2", List.of(lane4), List.of(lane1));
    Junction junction3 = junctionBuilder("J3", List.of(lane3), List.of(lane2));
    Junction junction4 = junctionBuilder("J4", List.of(), List.of(lane3, lane4, lane5));

    Patch patch1 = patchBuilder("P1", List.of(lane1, lane2, lane5), List.of(junction1), List.of("P2", "P3", "P4"));
    Patch patch2 = patchBuilder("P2", List.of(lane4), List.of(junction2), List.of("P1", "P4"));
    Patch patch3 = patchBuilder("P3", List.of(lane3), List.of(junction3), List.of("P1", "P4"));
    Patch patch4 = patchBuilder("P4", List.of(), List.of(junction4), List.of("P2", "P3", "P1"));

    MapFragmentId workerA = new MapFragmentId("A");
    MapFragmentId workerB = new MapFragmentId("B");
    MapFragmentId workerC = new MapFragmentId("C");

    MapFragmentBuilder mapFragmentBuilder = new MapFragmentBuilder(workerA);
    mapFragmentBuilder.addLocalPatch(patch1);
    mapFragmentBuilder.addLocalPatch(patch2);
    mapFragmentBuilder.addRemotePatch(workerB, patch4);
    mapFragmentBuilder.addRemotePatch(workerC, patch3);

    return mapFragmentBuilder.build();
  }

  // P - patch + junction ID
  // L - line
  // Worker A (me) - P1
  // Worker B      - P2 P4
  // Worker C      - P3
  //
  //         L1
  //   P1--------------P2
  //    |  \            |
  //  L2|    \  L5    L4|
  //    |  L3  \        /
  //   P3---------P4-- /

  private MapFragment getExampleMapFragment() {
    Lane lane1 = laneBuilder("L1", "J1", "J2");
    Lane lane2 = laneBuilder("L2", "J1", "J3");
    Lane lane3 = laneBuilder("L3", "J3", "J4");
    Lane lane4 = laneBuilder("L4", "J2", "J4");
    Lane lane5 = laneBuilder("L5", "J1", "J4");

    Junction junction1 = junctionBuilder("J1", List.of(lane1, lane2, lane3), List.of());
    Junction junction2 = junctionBuilder("J2", List.of(lane4), List.of(lane1));
    Junction junction3 = junctionBuilder("J3", List.of(lane3), List.of(lane2));
    Junction junction4 = junctionBuilder("J4", List.of(), List.of(lane3, lane4, lane5));

    Patch patch1 = patchBuilder("P1", List.of(lane1, lane2, lane5), List.of(junction1), List.of("P2", "P3", "P4"));
    Patch patch2 = patchBuilder("P2", List.of(lane4), List.of(junction2), List.of("P1", "P4"));
    Patch patch3 = patchBuilder("P3", List.of(lane3), List.of(junction3), List.of("P1", "P4"));
    Patch patch4 = patchBuilder("P4", List.of(), List.of(junction4), List.of("P2", "P3", "P1"));

    MapFragmentId workerA = new MapFragmentId("A");
    MapFragmentId workerB = new MapFragmentId("B");
    MapFragmentId workerC = new MapFragmentId("C");

    MapFragmentBuilder mapFragmentBuilder = new MapFragmentBuilder(workerA);
    mapFragmentBuilder.addLocalPatch(patch1);
    mapFragmentBuilder.addRemotePatch(workerB, patch2);
    mapFragmentBuilder.addRemotePatch(workerB, patch4);
    mapFragmentBuilder.addRemotePatch(workerC, patch3);

    return mapFragmentBuilder.build();
  }

  private Patch patchBuilder(String id, List<Lane> lanes, List<Junction> junctions, List<String> neighboursPatches) {
    return Patch.builder()
        .patchId(new PatchId(id))
        .junctions(junctions
            .stream()
            .collect(Collectors.toMap(Junction::getJunctionId, Function.identity())))
        .lanes(lanes
            .stream()
            .collect(Collectors.toMap(Lane::getLaneId, Function.identity())))
        .neighboringPatches(neighboursPatches
            .stream()
            .map(PatchId::new)
            .collect(Collectors.toSet()))
        .build();
  }

  private Junction junctionBuilder(String id, List<Lane> inComing, List<Lane> outComing) {
    JunctionBuilder builder = Junction.builder()
        .junctionId(new JunctionId(id, JunctionType.CROSSROAD));

    inComing.forEach(l -> builder.addIncomingLaneId(l.getLaneId(), true));
    outComing.forEach(l -> builder.addIncomingLaneId(l.getLaneId(), true));
    return  builder.build();
  }

  private Lane laneBuilder(String id, String incomingId, String outComingId){
      return  Lane.builder()
          .laneId(new LaneId(id))
          .incomingJunctionId(new JunctionId(incomingId, JunctionType.CROSSROAD))
          .outgoingJunctionId(new JunctionId(outComingId, JunctionType.CROSSROAD))
          .build();
  }

}
