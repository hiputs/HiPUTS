package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

/**
 * <p>This class uses the following naming convention for Patches and their status
 * from the current MapFragment point of view:</p>
 * <ul>
 *     <li>known Patches - visible to this MapFragment,</li>
 *     <li>shadow Patches - managed by neighboring MapFragment; subset of known Patches,</li>
 *     <li>local Patches - managed by this MapFragment; subset of known Patches,</li>
 *     <li>internal Patches - having only other local Patches as their neighbors; subset of local Patches,</li>
 *     <li>border Patches - having some shadow Patches as their neighbors; subset of local Patches.</li>
 * </ul>
 */
@AllArgsConstructor
public class MapFragment implements TransferDataHandler, RoadStructureReader, RoadStructureEditor {

  /**
   * Identifier of this MapFragment
   */
  @Getter
  private final MapFragmentId mapFragmentId;

  /**
   * All patches known by this MapFragment: local patches and shadow patches
   */
  private final Map<PatchId, Patch> knownPatches;

  /**
   * Local patches owned by this MapFragment
   */
  private final Set<PatchId> localPatchIds;

  /**
   * Border patches owned by this MapFragment grouped by neighbor MapFragment
   * Note: one border Patch can be assigned to multiple MapFragmentIds in this mapping.
   */
  private final Map<MapFragmentId, Set<PatchId>> mapFragmentIdToBorderPatchIds;

  /**
   * Mapping from neighbor MapFragmentIds to sets of PatchIds, corresponding to ownership of shadow Patches
   */
  private final Map<MapFragmentId, Set<PatchId>> mapFragmentIdToShadowPatchIds;

  /**
   * Mapping from LaneId to PatchId of Patch containing this Lane
   */
  private final Map<LaneId, PatchId> laneIdToPatchId;

  /**
   * Mapping from JunctionId to PatchId of Patch containing this Junction
   */
  private final Map<JunctionId, PatchId> junctionIdToPatchId;

  public static MapFragmentBuilder builder(MapFragmentId mapFragmentId) {
    return new MapFragmentBuilder(mapFragmentId);
  }

  @Override
  public LaneReadable getLaneReadable(LaneId laneId) {
    return Optional.ofNullable(laneIdToPatchId.get(laneId))
        .map(knownPatches::get)
        .map(patch -> patch.getLaneReadable(laneId))
        .orElse(null);
  }

  @Override
  public JunctionReadable getJunctionReadable(JunctionId junctionId) {
    return Optional.ofNullable(junctionIdToPatchId.get(junctionId))
        .map(knownPatches::get)
        .map(patch -> patch.getJunctionReadable(junctionId))
        .orElse(null);
  }

  @Override
  public LaneEditable getLaneEditable(LaneId laneId) {
    return Optional.ofNullable(laneIdToPatchId.get(laneId))
        .map(knownPatches::get)
        .map(patch -> patch.getLaneEditable(laneId))
        .orElse(null);
  }

  @Override
  public JunctionEditable getJunctionEditable(JunctionId junctionId) {
    return Optional.ofNullable(junctionIdToPatchId.get(junctionId))
        .map(knownPatches::get)
        .map(patch -> patch.getJunctionEditable(junctionId))
        .orElse(null);
  }

  @Override
  public Set<MapFragmentId> getNeighbors() {
    return mapFragmentIdToShadowPatchIds.keySet();
  }

  @Override
  public Map<MapFragmentId, Set<CarReadable>> pollOutgoingCars() {
    return mapFragmentIdToShadowPatchIds.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
            .stream()
            .map(knownPatches::get)
            .flatMap(Patch::streamLanesReadable)
            .flatMap(LaneReadable::streamCarsFromExitReadable)
            .collect(Collectors.toSet())));
  }

  @Override
  public void acceptIncomingCars(Set<Car> incomingCars) {
    incomingCars.forEach(car -> car.getLaneId().getEditable(this).addIncomingCar(car));
  }

  @Override
  public Map<MapFragmentId, Set<Patch>> getBorderPatches() {
    return mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            e -> e.getValue().stream().map(knownPatches::get).collect(Collectors.toSet())));
  }

  @Override
  public void acceptShadowPatches(Set<Patch> shadowPatches) {
    shadowPatches.forEach(shadowPatch -> knownPatches.put(shadowPatch.getPatchId(), shadowPatch));
  }

  @Override
  public Set<PatchReader> getKnownPatchReadable() {
    return knownPatches.values()
        .stream()
        .map(patch -> (PatchReader) patch)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<PatchReader> getShadowPatchesReadable() {
    return knownPatches.values()
        .stream()
        .filter(id -> !localPatchIds.contains(id))
        .map(patch -> (PatchReader) patch)
        .collect(Collectors.toSet());
  }

  public Set<LaneId> getLocalLaneIds() {
    return localPatchIds.stream()
        .map(knownPatches::get)
        .flatMap(patch -> patch.getLaneIds().stream())
        .collect(Collectors.toSet());
  }

  public Set<JunctionId> getLocalJunctionIds() {
    return localPatchIds.stream()
        .map(knownPatches::get)
        .flatMap(patch -> patch.getJunctionIds().stream())
        .collect(Collectors.toSet());
  }

  // TODO fix for new structure
  //    public void migrateMyPatchToNeighbour(PatchId patchId, ActorId receiver) {
  //        Patch patch = localPatches.remove(patchId);
  //        remotePatches.put(patchId, patch);
  //
  //        patch2Actor.put(patchId, receiver);
  //        refreshBorderPatches();
  //    }
  //
  //    private void refreshBorderPatches() {
  //        // all patches adjacent with remote patches
  //        Set<PatchId> neighbourPatch = remotePatches
  //                .values()
  //                .stream()
  //                .map(Patch::getNeighboringPatches)
  //                .flatMap(Set::stream)
  //                .collect(Collectors.toSet());
  //
  //        // our patches which adjacent with remote patches
  //        borderPatches = localPatches.values()
  //                .parallelStream()
  //                .filter(patch -> neighbourPatch.contains(patch.getId()))
  //                .collect(Collectors.toMap(Patch::getId, Function.identity()));
  //    }
  //
  //    public void migratePatchToMe(PatchId patchId) {
  //        Patch patch = remotePatches.remove(patchId);
  //        localPatches.put(patchId, patch);
  //
  //        patch2Actor.remove(patchId);
  //        refreshBorderPatches();
  //    }

  public static final class MapFragmentBuilder {

    private final MapFragmentId mapFragmentId;
    private final Map<PatchId, Patch> knownPatches = new HashMap<>();
    private final Set<PatchId> localPatchIds = new HashSet<>();
    private final Map<MapFragmentId, Set<PatchId>> shadowPatches = new HashMap<>();
    private final Map<PatchId, MapFragmentId> shadowPatchOwnership = new HashMap<>();

    public MapFragmentBuilder(MapFragmentId mapFragmentId) {
      this.mapFragmentId = mapFragmentId;
    }

    public MapFragmentBuilder addLocalPatch(Patch patch) {
      knownPatches.put(patch.getPatchId(), patch);
      localPatchIds.add(patch.getPatchId());
      return this;
    }

    public MapFragmentBuilder addRemotePatch(MapFragmentId mapFragmentId, Patch patch) {
      knownPatches.put(patch.getPatchId(), patch);
      shadowPatches.computeIfAbsent(mapFragmentId, k -> new HashSet<>()).add(patch.getPatchId());
      shadowPatchOwnership.put(patch.getPatchId(), mapFragmentId);
      return this;
    }

    public MapFragment build() {
      Map<MapFragmentId, Set<PatchId>> borderPatches = buildBorderPatches();

      Map<LaneId, PatchId> laneToPatch = knownPatches.values()
          .stream()
          .map(patch -> patch.getLaneIds()
              .stream()
              .collect(Collectors.toMap(Function.identity(), laneId -> patch.getPatchId())))
          .flatMap(map -> map.entrySet().stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      Map<JunctionId, PatchId> junctionToPatch = knownPatches.values()
          .stream()
          .map(patch -> patch.getJunctionIds()
              .stream()
              .collect(Collectors.toMap(Function.identity(), junctionId -> patch.getPatchId())))
          .flatMap(map -> map.entrySet().stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      return new MapFragment(mapFragmentId, knownPatches, localPatchIds, borderPatches, shadowPatches, laneToPatch,
          junctionToPatch);
    }

    /**
     * For each local patch, find neighboring patches that are not local (shadow patches),
     * get their owner MapFragmentIds and add the local patch as a border to this MapFragmentId
     */
    private Map<MapFragmentId, Set<PatchId>> buildBorderPatches() {
      Map<MapFragmentId, Set<PatchId>> borderPatches = new HashMap<>();
      localPatchIds.forEach(patchId -> knownPatches.get(patchId)
          .getNeighboringPatches()
          .stream()
          .filter(neighborPatch -> !localPatchIds.contains(neighborPatch))
          .map(shadowPatchOwnership::get)
          .forEach(neighborMapFragmentId -> borderPatches.computeIfAbsent(neighborMapFragmentId, k -> new HashSet<>())
              .add(patchId)));
      return borderPatches;
    }
  }
}
