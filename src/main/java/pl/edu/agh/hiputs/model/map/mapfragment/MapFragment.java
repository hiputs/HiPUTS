package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

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
            e -> e.getValue()
                .stream()
                .map(knownPatches::get)
                .collect(Collectors.toSet())));
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

  @Override
  public void migratePatchToNeighbour(Patch patch, MapFragmentId mapFragmentId) {
    // remove from local patches
    localPatchIds.remove(patch.getPatchId());

    //remove from border patches
    mapFragmentIdToBorderPatchIds
        .values()
        .forEach(patches -> patches.remove(patch.getPatchId()));

    // add new patches into border patches
    List<PatchId> newBorderPatchesAfterTransfer = patch.getNeighboringPatches()
        .stream()
        .filter(localPatchIds::contains)
        .toList();
    mapFragmentIdToBorderPatchIds.get(mapFragmentId).addAll(newBorderPatchesAfterTransfer);

    //add removed patch into shadow patches
    mapFragmentIdToShadowPatchIds.get(mapFragmentId).add(patch.getPatchId());

    //remove shadow patches - patch should be removed from shadow patches when no neighbors are adjacent to localPatches
    List<Patch> shadowPatchesToRemove = patch.getNeighboringPatches()
        .stream()
        .map(knownPatches::get)
        .filter(shadowPatch ->
            shadowPatch.getNeighboringPatches()
                .stream()
                .anyMatch(id -> !localPatchIds.contains(id)))
        .toList();

    shadowPatchesToRemove.forEach(id -> {
      Patch removedPatch = knownPatches.remove(id.getPatchId());

      removedPatch.getLaneIds()
          .forEach(laneIdToPatchId::remove);

      removedPatch.getJunctionIds()
          .forEach(junctionIdToPatchId::remove);
    });

    mapFragmentIdToShadowPatchIds.forEach(
        (key, value) -> shadowPatchesToRemove.forEach(c -> value.remove(c.getPatchId())));
  }

  public void migratePatchToMe(PatchId patchId, MapFragmentId neighbourId, MapRepository mapRepository, List<ImmutablePair<PatchId, MapFragmentId>> neighbourPatchIdsWithMapFragmentId) {
    Patch patch = knownPatches.get(patchId);
    // add to local patches
    localPatchIds.add(patch.getPatchId());

    //add to border patches
    neighbourPatchIdsWithMapFragmentId.forEach(
        pair -> {
          if(pair.getRight() == mapFragmentId){
            return;
          }

          Set<PatchId> neighbouring =
              mapFragmentIdToBorderPatchIds.computeIfAbsent(pair.getRight(), k -> new HashSet<>());

          neighbouring.add(pair.getLeft());
        }
    );

    // remove patches from border that have become internal after migration
    List<PatchId> incomePatch = patch.getNeighboringPatches()
        .stream()
        .filter(localPatchIds::contains)
        .filter(candidatePatchId ->
          localPatchIds.containsAll(knownPatches
              .get(candidatePatchId).getNeighboringPatches()))
        .toList();

    incomePatch
        .forEach(mapFragmentIdToBorderPatchIds.get(neighbourId)::remove);

    //removed patch from shadow patches
    mapFragmentIdToShadowPatchIds.get(neighbourId).remove(patch.getPatchId());

    //add shadow patches - patch should be added to shadow patches
    List<Patch> shadowPatchesToAdd = patch.getNeighboringPatches()
        .stream()
        .filter(id -> !knownPatches.containsKey(id))
        .map(mapRepository::getPatch)
        .toList();

    shadowPatchesToAdd.forEach(addedPatch -> {
      knownPatches.put(addedPatch.getPatchId(), addedPatch);

      addedPatch.getLaneIds()
          .forEach(laneId -> laneIdToPatchId.put(laneId, patchId));

      addedPatch.getJunctionIds()
          .forEach(junctionId -> junctionIdToPatchId.put(junctionId, patchId));
    });
    neighbourPatchIdsWithMapFragmentId.forEach(pair -> {
      if(pair.getValue().equals(mapFragmentId)){
        return;
      }

      Set<PatchId> shadowPatchIds = mapFragmentIdToShadowPatchIds.get(pair.getRight());
      if(shadowPatchIds == null){
        shadowPatchIds = new HashSet<>();
      }

      shadowPatchIds.add(pair.getLeft());
      mapFragmentIdToShadowPatchIds.put(pair.getRight(), shadowPatchIds);
    });
  }

  @Override
  public void migratePatchBetweenNeighbour(PatchId patchId, MapFragmentId source, MapFragmentId destination){
    mapFragmentIdToShadowPatchIds.get(source).remove(patchId);
    mapFragmentIdToShadowPatchIds.get(destination).add(patchId);

    mapFragmentIdToBorderPatchIds.get(source).remove(patchId);
    mapFragmentIdToBorderPatchIds.get(destination).add(patchId);

    Patch migratedPatch = knownPatches.get(patchId);

    Map<PatchId, Long> patchConnectionCounter = mapFragmentIdToShadowPatchIds.get(source)
        .stream()
        .map(knownPatches::get)
        .map(Patch::getNeighboringPatches)
        .flatMap(Collection::stream)
        .filter(id -> !localPatchIds.contains(id)) // we want only border patches
        .filter(knownPatches::containsKey) // and only known neighbouring
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    migratedPatch.getNeighboringPatches().forEach(id -> {
      if(patchConnectionCounter.get(id) != null && patchConnectionCounter.get(id) == 1){
        mapFragmentIdToShadowPatchIds.get(source).remove(id);
        mapFragmentIdToBorderPatchIds.get(source).remove(id);
      }
      mapFragmentIdToShadowPatchIds.get(destination).add(id);
      mapFragmentIdToBorderPatchIds.get(destination).add(id);
    });

  }

  @Override
  public Patch getPatchById(PatchId patchId) {
    return knownPatches.get(patchId);
  }

  @Override
  public MapFragmentId getMapFragmentIdByPatchId(PatchId patchId){
    if(localPatchIds.contains(patchId)){
      return mapFragmentId;
    }

    for (Map.Entry<MapFragmentId, Set<PatchId>> entry : mapFragmentIdToShadowPatchIds.entrySet()) {
      if(entry.getValue().contains(patchId)){
        return entry.getKey();
      }
    }

    throw new RuntimeException("Not found mapFragmentId");
  }

  @Override
  public MapFragmentId getMe() {
    return mapFragmentId;
  }

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

      Map<JunctionId, PatchId> junctionToPatch = new HashMap<>();

      knownPatches.values().forEach(patch -> {
        patch.getJunctionIds()
            .forEach(junctionId -> junctionToPatch.put(junctionId, patch.getPatchId()));
      });

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
