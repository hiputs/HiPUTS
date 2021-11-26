package model.map;

import lombok.Getter;
import lombok.Setter;
import model.id.JunctionId;
import model.id.LaneId;
import model.id.PatchId;

import java.util.Map;

@Getter
@Setter
public class Patch {
    private PatchId id;

    /**
     * Junctions within this patch
     */
    private Map<JunctionId, Junction> junctions;
    /**
     * Lanes within this patch
     */
    private Map<LaneId, LaneLocal> lanes;


    /**
     * Patches that are adjacent/neighbours to this patch
     */
    private Map<PatchId, Patch> neighboringPatches;
}
