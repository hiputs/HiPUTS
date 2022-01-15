package pl.edu.agh.model.actor;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.ActorId;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.id.PatchId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.LaneReadOnly;
import pl.edu.agh.model.map.Patch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActorContext implements RoadStructureProvider {

    /**
     * All patches within this ActorContext - they represent patches that are situated within the same JVM
     */
    private Map<PatchId, Patch> localPatches;

    /**
     * All remote patches (shadow patches from neighboring Actor Contexts) to this ActorContext
     */
    private Map<PatchId, Patch> remotePatches;

    /**
     * Lane to patch mapper
     */
    private Map<LaneId, PatchId> lane2Patch;

    /**
     * Neighbors that have at least one directly connected junctions
     */
    private Set<ActorId> neighbours;

    /**
     * Actor to patch mapper
     */
    private Map<ActorId, PatchId> actor2Patch;

    @Override
    public LaneReadOnly getLane(LaneId laneId) {
        throw new UnsupportedOperationException("method not implemented!");
    }

    @Override
    public Junction getJunction(JunctionId junctionId) {
        throw new UnsupportedOperationException("method not implemented!");
    }

    public static Builder builder() {
        return new Builder();
    }

    private void stage(Car car) {
        throw new UnsupportedOperationException("method not implemented!");
        // Add car to some collection for future sending
    }

    public static final class Builder {
        private Map<PatchId, Patch> localPatches = new HashMap<>();
        private Map<PatchId, Patch> remotePatches = new HashMap<>();

        public Builder addLocalPatch(Patch localPatch) {
            this.localPatches.put(localPatch.getId(), localPatch);
            return this;
        }

        public Builder addRemotePatch(Patch remotePatch) {
            this.remotePatches.put(remotePatch.getId(), remotePatch);
            return this;
        }

        public ActorContext build() {
            ActorContext actorContext = new ActorContext();
            actorContext.localPatches = this.localPatches;
            actorContext.remotePatches = this.remotePatches;
            actorContext.lane2Patch = Stream.concat(
                            this.localPatches.values().stream(),
                            this.remotePatches.values().stream())
                    .map(patch -> patch.getLanes()
                            .keySet().stream()
                            .collect(Collectors.toMap(Function.identity(), laneId -> patch.getId())))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return actorContext;
        }
    }
}