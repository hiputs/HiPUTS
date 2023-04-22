package pl.edu.agh.utils;

import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.utils.DeterminingNeighborhoodUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeterminingNeighbourhoodUtilTest {

    @Test
    void shouldDesignateNeighborsWhenExist2Patches() {
        List<Patch> patches = genSmallMap();

        DeterminingNeighborhoodUtil.execute(patches);

        assertTrue(patches.get(0).getNeighboringPatches().contains(new PatchId("PATCH_2")));
        assertTrue(patches.get(1).getNeighboringPatches().contains(new PatchId("PATCH_1")));
        assertEquals(1, patches.get(0).getNeighboringPatches().size());
        assertEquals(1, patches.get(1).getNeighboringPatches().size());
    }

    @Test
    void shouldDesignateNeighborsWhenExist3Patches() {
        List<Patch> patches = genLargeMap();

        DeterminingNeighborhoodUtil.execute(patches);

        assertTrue(patches.get(0).getNeighboringPatches().contains(new PatchId("PATCH_2")));
        assertTrue(patches.get(0).getNeighboringPatches().contains(new PatchId("PATCH_3")));
        assertTrue(patches.get(1).getNeighboringPatches().contains(new PatchId("PATCH_1")));
        assertTrue(patches.get(2).getNeighboringPatches().contains(new PatchId("PATCH_1")));

        assertEquals(2, patches.get(0).getNeighboringPatches().size());
        assertEquals(1, patches.get(1).getNeighboringPatches().size());
    }

    //  It's look like
    //      ----> P2
    //     /
    //   P1
    //     \
    //      ----> P3
    private List<Patch> genLargeMap() {
        Road lane = Road.builder()
            .roadId(new RoadId("Lane1"))
            .incomingJunctionId(new JunctionId("1", JunctionType.BEND))
            .outgoingJunctionId(new JunctionId("2", JunctionType.BEND))
            .build();

        Road lane2 = Road.builder()
            .roadId(new RoadId("Lane2"))
            .incomingJunctionId(new JunctionId("3", JunctionType.BEND))
            .outgoingJunctionId(new JunctionId("1", JunctionType.BEND))
            .build();

        Patch patch1 = Patch.builder()
            .patchId(new PatchId("PATCH_1"))
            .junctions(Map.of(new JunctionId("1", JunctionType.BEND), Junction.builder().build()))
            .roads(Map.of(
                lane.getRoadId(), lane,
                lane2.getRoadId(), lane2))
            .build();

        Patch patch2 = Patch.builder()
            .patchId(new PatchId("PATCH_2"))
            .junctions(Map.of(new JunctionId("2", JunctionType.BEND), Junction.builder().build()))
            .roads(Map.of(lane.getRoadId(), lane))
            .build();

        Patch patch3 = Patch.builder()
            .patchId(new PatchId("PATCH_3"))
            .junctions(Map.of(new JunctionId("3", JunctionType.BEND), Junction.builder().build()))
            .roads(Map.of(lane2.getRoadId(), lane2))
            .build();

        return List.of(patch1, patch2, patch3);
    }

    //  It's look like
    //  P1 ----> P2
    private static List<Patch> genSmallMap() {
        Road lane = Road.builder()
            .roadId(new RoadId("Lane1"))
            .incomingJunctionId(new JunctionId("2", JunctionType.BEND))
            .outgoingJunctionId(new JunctionId("1", JunctionType.BEND))
            .build();

        Patch patch1 = Patch.builder()
            .patchId(new PatchId("PATCH_1"))
            .junctions(Map.of(new JunctionId("1", JunctionType.BEND), Junction.builder().build()))
            .roads(Map.of(lane.getRoadId(), lane))
            .build();

        Patch patch2 = Patch.builder()
            .patchId(new PatchId("PATCH_2"))
            .junctions(Map.of(new JunctionId("2", JunctionType.BEND), Junction.builder().build()))
            .roads(Map.of(lane.getRoadId(), lane))
            .build();

        return List.of(patch1, patch2);
    }
}
