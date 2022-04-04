package pl.edu.agh.hiputs.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class Patch implements PatchRead, PatchReadWrite{
    private final PatchId id;

    /**
     * Junctions within this patch
     */
    private Map<JunctionId, Junction> junctions;

    /**
     * Lanes within this patch
     */
    private Map<LaneId, Lane> lanes;


    /**
     * Patches that are adjacent/neighbours to this patch
     */
    private Set<PatchId> neighboringPatches;

    public Patch() {
        this(new PatchId());
    }

    @Override
    public LaneRead getLaneReadById(LaneId laneId) {
        return lanes.get(laneId);
    }

    @Override
    public JunctionRead getJunctionReadById(JunctionId junctionId) {
        return junctions.get(junctionId);
    }

    @Override
    public LaneReadWrite getLaneReadWriteById(LaneId laneId) {
        return lanes.get(laneId);
    }

    @Override
    public JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId) {
        return junctions.get(junctionId);
    }

    @Override
    public Set<LaneId> getLaneIds() {
        return lanes.keySet();
    }

    @Override
    public Set<JunctionId> getJunctionIds() {
        return junctions.keySet();
    }
}
