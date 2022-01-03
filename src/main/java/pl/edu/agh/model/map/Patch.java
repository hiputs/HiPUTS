package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.id.PatchId;

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
