package pl.edu.agh.hiputs.model.map.mapfragment;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchEditor;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.MapStatistic;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.utils.DebugUtils;

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
@Slf4j
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
  public List<LaneEditable> getRandomLanesEditable(int count) {
    Set<LaneEditable> lanes = new HashSet<>();
    Object[] array = localPatchIds.toArray();

    do {
      PatchId patchId = (PatchId) array[ThreadLocalRandom.current().nextInt(0, array.length)];
      Patch patch = knownPatches.get(patchId);
      lanes.addAll(patch.getLaneIds().parallelStream().map(patch::getLaneEditable).toList());
    } while (lanes.size() < count);

    return new ArrayList<>(lanes);

  }

  @Override
  public Set<MapFragmentId> getNeighbors() {
    return getBorderPatches().keySet();
  }

  @Override
  public Map<MapFragmentId, Set<CarEditable>> pollOutgoingCars() {
    return mapFragmentIdToShadowPatchIds.entrySet()
        .parallelStream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
            .parallelStream()
            .map(knownPatches::get)
            .flatMap(Patch::streamLanesEditable)
            .flatMap(LaneEditable::pollIncomingCars)
            .collect(Collectors.toSet())));
  }

  @Override
  public void acceptIncomingCars(Set<Car> incomingCars) {
    incomingCars.parallelStream().peek(car -> {
      LaneEditable lane = car.getDecision().getLaneId().getEditable(this);
      if (lane != null) {
        lane.addIncomingCar(car);
      } else {
        log.warn("Not found lane {}, patchId {}", car.getDecision().getLaneId(), DebugUtils.getMapRepository().getPatchIdByLaneId( car.getDecision().getLaneId()).getValue());
      }

    }).toList();
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
    return knownPatches.values().stream().map(patch -> (PatchReader) patch).collect(Collectors.toSet());
  }

  @Override
  public Set<PatchReader> getShadowPatchesReadable() {
    return knownPatches.values()
        .stream()
        .filter(patch -> !localPatchIds.contains(patch.getPatchId()))
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
  public PatchEditor getShadowPatchEditableCopy(PatchId patchId) {
    //todo it should be deepcopy in here
    return knownPatches.get(patchId);
  }

  public int getMyPatchCount() {
    return localPatchIds.size();
  }

  @Override
  public void migratePatchToNeighbour(Patch patch, MapFragmentId neighbourId) {
    log.info("migrate to nieghbours patch {} id {}", patch.getPatchId().getValue(), neighbourId.getId());
    // remove from local patches
    localPatchIds.remove(patch.getPatchId());

    //remove from border patches
    mapFragmentIdToBorderPatchIds.values().forEach(patches -> patches.remove(patch.getPatchId()));

    // add new patches into border patches
    List<PatchId> newBorderPatchesAfterTransfer =
        PatchConnectionSearchUtil.findNeighbouringPatches(patch.getPatchId(), this);
    mapFragmentIdToBorderPatchIds.get(neighbourId).addAll(newBorderPatchesAfterTransfer);

    //add removed patch into shadow patches
    boolean isEndPatch = PatchConnectionSearchUtil.isEndPatch(patch, mapFragmentId, this);
    if (!isEndPatch){
      mapFragmentIdToShadowPatchIds.get(neighbourId).add(patch.getPatchId());
    }

    //remove shadow patches - patch should be removed from shadow patches when no neighbors are adjacent to localPatches
    List<PatchId> shadowPatchesToRemove =
        PatchConnectionSearchUtil.findShadowPatchesNeighbouringOnlyWithPatch(patch.getPatchId(), this);

    log.info("shadow -> deleted patches {} -> {}", shadowPatchesToRemove.size(),
        shadowPatchesToRemove
            .stream()
            .map(PatchId::getValue)
            .collect(Collectors.joining(", ")));

    shadowPatchesToRemove.forEach(this::removePatch);

    mapFragmentIdToShadowPatchIds.forEach((key, value) -> shadowPatchesToRemove.forEach(value::remove));
    removeEmptyNeighbours();
  }

  public void migratePatchToMe(PatchId patchId, MapFragmentId neighbourId, MapRepository mapRepository,
      List<ImmutablePair<PatchId, MapFragmentId>> neighbourPatchIdsWithMapFragmentId) {
    log.info("I got patchId {}  from {}", patchId.getValue(), neighbourId.getId());
    Patch patch = knownPatches.get(patchId);

    if (patch == null) {
      patch = addPatch(mapRepository, patchId);
    }
    // add to local patches
    localPatchIds.add(patch.getPatchId());

    // add to border patches
    if(mapFragmentIdToBorderPatchIds.get(neighbourId) == null){
      log.info("create new collections for nieghbourId {}", neighbourId);
    }
    mapFragmentIdToBorderPatchIds.computeIfAbsent(neighbourId, k -> new HashSet<>());
    boolean isLastConnectionPatch = PatchConnectionSearchUtil.isEndPatch(patch, neighbourId, this);
    log.info("isLastConnection {}", isLastConnectionPatch);
    if (!isLastConnectionPatch) {
      mapFragmentIdToBorderPatchIds.get(neighbourId).add(patchId);
    }

    // log.debug("migrate to local patch {} from worker {}", patchId.getValue(), neighbourId.getId());

    // remove patches from border that have become internal after migration
    List<PatchId> incomePatch =
        patch.getNeighboringPatches()
            .parallelStream()
            .filter(localPatchIds::contains)
            .filter(candidatePatchId -> {
                Patch checkingNeighbour = knownPatches.get(candidatePatchId);
                return checkingNeighbour.getNeighboringPatches()
                    .stream()
                    .noneMatch(id -> neighbourId.equals(getMapFragmentIdByPatchId(id)));
        }).toList();

    log.info("border-> income patches {} -> {}", incomePatch.size(),
        incomePatch.stream().map(PatchId::getValue).collect(Collectors.joining(", ")));
    incomePatch.forEach(id -> {
      mapFragmentIdToBorderPatchIds.get(neighbourId).remove(id);
    });

    //removed patch from shadow patches
    mapFragmentIdToShadowPatchIds.values().forEach(set -> set.remove(patchId));
    // mapFragmentIdToShadowPatchIds.get(neighbourId).remove(patch.getPatchId());

    //add shadow patches - patch should be added to shadow patches

    List<ImmutablePair<PatchId, MapFragmentId>> shadowPatchesToAdd =
        neighbourPatchIdsWithMapFragmentId
            .stream()
            .filter(p -> !p.getRight().equals(mapFragmentId))
            .filter(p -> !localPatchIds.contains(p.getLeft()))
            .toList();

    shadowPatchesToAdd.forEach(p -> {
      addPatch(mapRepository, p.getLeft());
    });

    shadowPatchesToAdd.forEach(pair -> {

      mapFragmentIdToShadowPatchIds.computeIfAbsent(pair.getRight(), k -> new HashSet<>());
      log.info("Add shadow patch {} to nieghbour {}", pair.getLeft().getValue(), pair.getRight().getId());
      mapFragmentIdToShadowPatchIds.values().forEach(p -> p.remove(pair.getLeft()));
      mapFragmentIdToShadowPatchIds.get(pair.getRight()).add(pair.getLeft());

      mapFragmentIdToBorderPatchIds.computeIfAbsent(pair.getRight(), k -> new HashSet<>());
      List<PatchId> newBorderPatches = PatchConnectionSearchUtil.findNeighbouringPatches(pair.getLeft(), this);
      mapFragmentIdToBorderPatchIds.get(pair.getRight()).addAll(newBorderPatches);
    });

    removeEmptyNeighbours();
  }

  @Override
  public void migratePatchBetweenNeighbour(PatchId patchId, MapFragmentId destination, MapFragmentId source) {
    log.info("handle migration patch between neighbours {} -> {}, {}", source.getId(), destination.getId(), patchId.getValue());
    if (!knownPatches.containsKey(patchId)) {
      return;
    }

    log.info("handle migration patch between neighbours {}", patchId.getValue());

    // change patches between neighbour
    if (mapFragmentIdToShadowPatchIds.get(source) != null) {
      mapFragmentIdToShadowPatchIds.get(source).remove(patchId);
    }

    mapFragmentIdToShadowPatchIds.computeIfAbsent(destination, k -> new HashSet<>());
    mapFragmentIdToShadowPatchIds.get(destination).add(patchId);

    // add new border patches into destination
    List<PatchId> localPatchesConnectedWith = PatchConnectionSearchUtil.findNeighbouringPatches(patchId, this);
    mapFragmentIdToBorderPatchIds.computeIfAbsent(destination, k -> new HashSet<>());
    mapFragmentIdToBorderPatchIds.get(destination).addAll(localPatchesConnectedWith);

    // remove border patches from source when not connected with neighbour
    if (mapFragmentIdToShadowPatchIds.get(source) == null) {
      return;
    }

    final Set<PatchId> allPatchesNeighbouringWithShadowPatches = mapFragmentIdToShadowPatchIds.get(source)
        .parallelStream()
        .map(knownPatches::get)
        .flatMap(p -> p.getNeighboringPatches().stream())
        .collect(Collectors.toSet());

    final Set<PatchId> newBorderPatchesSet =
        allPatchesNeighbouringWithShadowPatches.parallelStream().filter(this::isLocalPatch).collect(Collectors.toSet());

    log.info("****Add borderPatches**** {} { {} }", source.getId(),
        newBorderPatchesSet.stream().map(PatchId::getValue).collect(Collectors.joining(", ")));
    mapFragmentIdToBorderPatchIds.put(source, newBorderPatchesSet);

    removeEmptyNeighbours();
  }

  @Override
  public Patch getPatchById(PatchId patchId) {
    return knownPatches.get(patchId);
  }

  @Override
  public MapFragmentId getMapFragmentIdByPatchId(PatchId patchId) {
    if (localPatchIds.contains(patchId)) {
      return mapFragmentId;
    }

    for (Map.Entry<MapFragmentId, Set<PatchId>> entry : mapFragmentIdToShadowPatchIds.entrySet()) {
      if (entry.getValue().contains(patchId)) {
        return entry.getKey();
      }
    }

    return null;

    // throw new RuntimeException("Not found mapFragmentId");
  }

  private Patch addPatch(MapRepository mapRepository, PatchId patchId) {

    if (knownPatches.containsKey(patchId)) {
      return knownPatches.get(patchId);
    }

    Patch addedPatch = mapRepository.getPatch(patchId);
    knownPatches.put(addedPatch.getPatchId(), addedPatch);

    addedPatch.getLaneIds().forEach(laneId -> laneIdToPatchId.put(laneId, addedPatch.getPatchId()));
    addedPatch.getJunctionIds().forEach(junctionId -> junctionIdToPatchId.put(junctionId, addedPatch.getPatchId()));
    return addedPatch;
  }

  private void removePatch(PatchId id) {

    log.info("Remove patch {}", id.getValue());
    Patch removedPatch = knownPatches.remove(id);
    mapFragmentIdToShadowPatchIds.values().forEach(set -> set.remove(id));
    localPatchIds.remove(id);

    removedPatch.getLaneIds().forEach(laneIdToPatchId::remove);

    removedPatch.getJunctionIds().forEach(junctionIdToPatchId::remove);
  }

  private void removeEmptyNeighbours() {
    List<MapFragmentId> lostConnectNeighbours = mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .filter(i -> i.getValue().isEmpty())
        .map(Entry::getKey)
        .toList();

    log.info("Me {}, Remove empty neighbours {}", mapFragmentId.getId(),
        lostConnectNeighbours.stream().map(MapFragmentId::getId).collect(Collectors.joining(", ")));

    lostConnectNeighbours.forEach(n -> {
      mapFragmentIdToBorderPatchIds.remove(n);

      if (mapFragmentIdToShadowPatchIds.get(n).size() == 0) {
        log.info("Remove shadow too");
        mapFragmentIdToShadowPatchIds.remove(n);
      }
    });
  }

  @Override
  public MapFragmentId getMe() {
    return mapFragmentId;
  }

  @Override
  public boolean isLocalPatch(PatchId patchId) {
    return localPatchIds.contains(patchId);
  }

  @Override
  public PatchId getPatchIdByLaneId(LaneId laneId) {
    return laneIdToPatchId.get(laneId);
  }

  @Override
  public void printStaistic() {
    log.info("Local patches {}, borderPatches {} ", localPatchIds.size(),
        mapFragmentIdToBorderPatchIds.values().stream().map(Set::size).reduce(0, Integer::sum));
  }

  public void printFullStatistic() {
    String localPatch = localPatchIds.stream()
        .map(PatchId::getValue)
        .collect(Collectors.joining(","));

    String borderPatches = mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .map(k -> k.getKey().getId() + " -> " + k.getValue().stream().map(PatchId::getValue)
            .sorted()
            .collect(Collectors.joining(",")))
        .collect(Collectors.joining("\n"));

    String shadowPatches = mapFragmentIdToShadowPatchIds.entrySet()
        .stream()
        .map(k -> k.getKey().getId() + " -> " + k.getValue().stream().map(PatchId::getValue)
            .sorted()
            .collect(Collectors.joining(",")))
        .collect(Collectors.joining("\n"));

    String response = "****************************\n"
        + "local: " + localPatch + "\n" +
        "border: \n"+ borderPatches + "\n" +
        "shadow: \n"+ shadowPatches + "\n" +
        "*********************************************";
    log.info(response);
  }

  public MapStatistic getMapStatistic(int step) {
    return MapStatistic.builder()
        .localPatches(localPatchIds.size())
        .borderPatches(getBorderPatches().values().stream().map(Set::size).reduce(0, Integer::sum))
        .workerId(mapFragmentId.getId())
        .shadowPatches(getShadowPatchesReadable().size())
        .neighbouring(getBorderPatches().entrySet()
            .stream()
            .filter(i -> i.getValue().size() > 0)
            .map(i -> new ImmutablePair<>(i.getKey().getId(), i.getValue().size()))
            .toList())
        .step(step)
        .build();
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
        patch.getJunctionIds().forEach(junctionId -> junctionToPatch.put(junctionId, patch.getPatchId()));
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
