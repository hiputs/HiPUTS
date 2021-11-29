package model.map;

import lombok.Getter;
import lombok.Setter;
import model.id.JunctionId;
import model.id.LaneId;
import model.id.PatchId;

import java.util.Map;
import java.util.Set;

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
    private Map<LaneId, LaneReadWrite> lanes;


    /**
     * Patches that are adjacent/neighbours to this patch
     */
    private Set<PatchId> neighboringPatches;
}
