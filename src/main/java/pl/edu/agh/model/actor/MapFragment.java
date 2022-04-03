package pl.edu.agh.model.actor;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.ActorId;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.id.PatchId;
import pl.edu.agh.model.map.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Getter
@Setter
public class MapFragment implements RoadStructureProvider, MapFragmentRead, MapFragmentReadWrite {

    /**
     * All patches within this MapFragment - they represent patches that are situated within the same JVM
     */
    private Map<PatchId, Patch> localPatches;

    /**
     * All remote patches (shadow patches from neighboring Actor Contexts) to this MapFragment
     */
    private Map<PatchId, Patch> remotePatches;

    /**
     * only areas immediately  neighbouring/another actor
     */
    private Map<PatchId, Patch> borderPatches;

    /**
     * Lane to patch mapper
     */
    private Map<LaneId, PatchId> lane2Patch;

    /**
     * Neighbors that have at least one directly connected junctions
     */
    @Getter
    private Set<ActorId> neighbours = new HashSet<>();

    /**
     * Patch to actor mapper
     */
    @Getter
    private Map<PatchId, ActorId> patch2Actor = new HashMap<>();

    public LaneReadWrite getLaneReadWrite(LaneId laneId){
        PatchId patchId= lane2Patch.get(laneId);
        return localPatches.get(patchId).getLanes().get(laneId);
    }

    @Override
    public LaneRead getLaneReadById(LaneId laneId) {
        return getLaneReadWriteById(laneId);
    }

    @Override
    public JunctionRead getJunctionReadById(JunctionId junctionId) {
        // todo improve performance
        Collection<Patch> allPatches = this.localPatches.values();
        allPatches.addAll(this.remotePatches.values());

        return allPatches.stream().filter(
                patch -> patch.getJunctions().containsKey(junctionId)
        ).findAny().get().getJunctions().get(junctionId);
    }

    public Collection<Patch> getLocalPatches() {
        return this.localPatches.values();
    }

    public Patch getLocalPatch(PatchId patchId){
        return localPatches.get(patchId);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void stage(Car car) {
        throw new UnsupportedOperationException("method not implemented!");
        // Add car to some collection for future sending
    }

    @Override
    public void addCar(LaneId laneId, Car car) {
        getLaneReadWriteById(laneId).addFirstCar(car);
    }

    @Override
    public Car removeLastCarFromLane(LaneId laneId) {
        return getLaneReadWriteById(laneId).removeLastCar();
    }

    @Override
    public LaneReadWrite getLaneReadWriteById(LaneId laneId) {
        PatchId patchId = lane2Patch.get(laneId);
        if (!isLocalPatch(patchId)) {
            throw new IllegalPatchWriteAccessException(
                    String.format("Lane with id %s cannot be modified from this map fragment", laneId.toString()));
        }
        return localPatches.get(patchId).getLanes().get(laneId);
    }

    @Override
    public JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public Set<LaneId> getAllManagedLaneIds() {
        return localPatches.values().stream()
                .flatMap(patch -> patch.getLanes().keySet().stream())
                .collect(Collectors.toSet());
    }

    private boolean isLocalPatch(PatchId patchId) {
        return localPatches.containsKey(patchId);
    }

    @SneakyThrows
    public void insertCar(Car car) {
        PatchId patchId = lane2Patch.get(car.getLocation().getLane());
        LaneReadWrite lane = localPatches.get(patchId).getLanes().get(car.getLocation().getLane());
        lane.addToIncomingCars(car);
    }

    public void migrateMyPatchToNeighbour(PatchId patchId, ActorId receiver) {
        Patch patch = localPatches.remove(patchId);
        remotePatches.put(patchId, patch);

        patch2Actor.put(patchId, receiver);
        refreshBorderPatches();
    }

    private void refreshBorderPatches() {
        Set<PatchId> neighbourPatch = localPatches
                .values()
                .stream()
                .map(Patch::getNeighboringPatches)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        List<PatchId> localPatch = localPatches
                .values()
                .stream()
                .map(Patch::getId)
                .collect(Collectors.toList());

        localPatch.forEach(neighbourPatch::remove);

        borderPatches = remotePatches.values()
                .parallelStream()
                .filter(patch -> neighbourPatch.contains(patch.getId()))
                .collect(Collectors.toMap(Patch::getId, Function.identity()));
    }

    public void migratePatchToMe(PatchId patchId) {
        Patch patch = remotePatches.remove(patchId);
        localPatches.put(patchId, patch);

        patch2Actor.remove(patchId);
        refreshBorderPatches();
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

        public MapFragment build() {
            MapFragment mapFragment = new MapFragment();
            mapFragment.localPatches = this.localPatches;
            mapFragment.remotePatches = this.remotePatches;
            mapFragment.patch2Actor = new HashMap<>();
            mapFragment.borderPatches = new HashMap<>();
            mapFragment.neighbours = new HashSet<>();
            mapFragment.lane2Patch = Stream.concat(
                            this.localPatches.values().stream(),
                            this.remotePatches.values().stream())
                    .map(patch -> patch.getLanes()
                            .keySet().stream()
                            .collect(Collectors.toMap(Function.identity(), laneId -> patch.getId())))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return mapFragment;
        }
    }
}
