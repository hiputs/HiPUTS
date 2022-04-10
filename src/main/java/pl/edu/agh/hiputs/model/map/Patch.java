package pl.edu.agh.hiputs.model.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class Patch implements PatchReader, PatchEditor {

    /**
     * Identifier of this Patch
     */
    @Getter
    @Builder.Default
    private final PatchId id = PatchId.random();

    /**
     * Junctions within this patch
     */
    private final Map<JunctionId, Junction> junctions;

    /**
     * Lanes within this patch
     */
    private final Map<LaneId, Lane> lanes;

    /**
     * Patches that are adjacent/neighbours to this patch
     */
    @Getter
    @Builder.Default
    private final Set<PatchId> neighboringPatches = new HashSet<>();

    @Override
    public Set<LaneId> getLaneIds() {
        return lanes.keySet();
    }

    @Override
    public LaneReadable getLaneReadable(LaneId laneId) {
        return lanes.get(laneId);
    }

    @Override
    public LaneEditable getLaneEditable(LaneId laneId) {
        return lanes.get(laneId);
    }

    @Override
    public Stream<LaneReadable> streamLanesReadable() {
        return lanes.values().stream().map(Function.identity());
    }

    @Override
    public Stream<LaneEditable> streamLanesEditable() {
        return lanes.values().stream().map(Function.identity());
    }

    @Override
    public Set<JunctionId> getJunctionIds() {
        return junctions.keySet();
    }

    @Override
    public JunctionReadable getJunctionReadable(JunctionId junctionId) {
        return junctions.get(junctionId);
    }

    @Override
    public JunctionEditable getJunctionEditable(JunctionId junctionId) {
        return junctions.get(junctionId);
    }

    @Override
    public Stream<JunctionReadable> streamJunctionsReadable() {
        return junctions.values().stream().map(Function.identity());
    }

    public Stream<JunctionEditable> streamJunctionsEditable() {
        return junctions.values().stream().map(Function.identity());
    }
}
