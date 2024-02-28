package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.loadbalancer.TicketService;
import pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchEditor;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.MapStatistic;
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
   * Mapping from RoadId to PatchId of Patch containing this Road
   */
  private final Map<RoadId, PatchId> roadIdToPatchId;

  /**
   * Mapping from RoadId to PatchId of Patch containing this Lane
   */
  private final Map<LaneId, PatchId> laneIdToPatchId;

  /**
   * Mapping from JunctionId to PatchId of Patch containing this Junction
   */
  private final Map<JunctionId, PatchId> junctionIdToPatchId;

  /**
   * Map of patches sent to neighbours during Load Balancing
   * should be cleared before each load balancing
   */
  private final Map<PatchId, MapFragmentId> sentPatchesIdToMapFragmentId;

  /**
   * Lis of neighbours removed due to LB process
   */
  private final List<MapFragmentId> removedNeighbours;

  public static MapFragmentBuilder builder(MapFragmentId mapFragmentId) {
    return new MapFragmentBuilder(mapFragmentId);
  }

  @Override
  public RoadReadable getRoadReadable(RoadId roadId) {
    return Optional.ofNullable(roadIdToPatchId.get(roadId))
        .map(knownPatches::get)
        .map(patch -> patch.getRoadReadable(roadId))
        .orElse(null);
  }

  @Override
  public LaneReadable getLaneReadable(LaneId laneId) {
    return Optional.ofNullable(laneIdToPatchId.get(laneId))
        .map(knownPatches::get)
        .map(patch -> patch.getLaneReadable(laneId))
        .orElse(null);
  }

  @Override
  public List<LaneReadable> getLaneSuccessorsReadable(LaneId laneId) {
    return getLaneReadable(laneId)
        .getLaneSuccessors()
        .stream()
        .map(this::getLaneReadable)
        .collect(Collectors.toList());
  }

  @Override
  public List<LaneReadable> getRoadToLaneSuccessorsReadable(RoadId roadId) {
    return getRoadReadable(roadId)
        .getLanes()
        .stream()
        .map(this::getLaneSuccessorsReadable)
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public List<LaneReadable> getLanesReadableFromRoadId(RoadId roadId) {
    return getRoadReadable(roadId)
        .getLanes()
        .stream()
        .map(this::getLaneReadable)
        .collect(Collectors.toList());
  }

  @Override
  public RoadReadable getRoadReadableFromLaneId(LaneId laneId) {
    return Optional.ofNullable(laneIdToPatchId.get(laneId))
        .map(knownPatches::get)
        .map(patch -> patch.getRoadReadable(patch.getLaneReadable(laneId).getRoadId()))
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
  public RoadEditable getRoadEditable(RoadId roadId) {
    return Optional.ofNullable(roadIdToPatchId.get(roadId))
        .map(knownPatches::get)
        .map(patch -> patch.getRoadEditable(roadId))
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
    List<LaneEditable> lanes = new ArrayList<>(count);
    Object[] array = localPatchIds.toArray();

    do {
      PatchId patchId = (PatchId) array[ThreadLocalRandom.current().nextInt(0, array.length)];
      Patch patch = knownPatches.get(patchId);
      lanes.addAll(patch.getLaneIds().stream().map(patch::getLaneEditable).toList());
    } while (lanes.size() < count);

    return new ArrayList<>(lanes);

  }

  @Override
  public Set<MapFragmentId> getNeighbors() {
    return getBorderPatches().keySet();
  }

  @Override
  public Map<MapFragmentId, Set<Patch>> getBorderPatches() {
    return mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            e -> e.getValue().stream().map(knownPatches::get).collect(Collectors.toSet())));
  }

  @Override
  public Map<MapFragmentId, Set<CarEditable>> pollOutgoingCars() {
    return mapFragmentIdToShadowPatchIds.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
            .map(knownPatches::get)
            .flatMap(Patch::streamLanesEditable)
            .flatMap(LaneEditable::pollIncomingCars)
            .collect(Collectors.toSet())));
  }

  @Override
  public void acceptIncomingCars(Set<Car> incomingCars) {
    incomingCars.forEach(car -> {
      LaneEditable lane = car.getDecision().getLaneId().getEditable(this);
      if (lane != null) {
        lane.addIncomingCar(car);
      } else {
        log.warn("Not found lane {} for load balanced car {}", car.getDecision().getLaneId(), car.getCarId());
      }
    });
  }

  @Override
  public synchronized void acceptShadowPatches(Set<Patch> shadowPatches) {
    shadowPatches.forEach(shadowPatch -> knownPatches.put(shadowPatch.getPatchId(), shadowPatch));
  }

  @Override
  public Set<PatchReader> getKnownPatchReadable() {
    return knownPatches.values().stream().map(patch -> (PatchReader) patch).collect(Collectors.toSet());
  }

  public Set<PatchEditor> getLocalPatchesEditable() {
    return knownPatches.values()
        .stream().filter(patch -> localPatchIds.contains(patch.getPatchId())).map(patch -> (PatchEditor) patch)
        .collect(Collectors.toSet());
  }

  public List<Patch> getLocalPatches() {
    return knownPatches.values()
        .stream()
        .filter(patch -> localPatchIds.contains(patch.getPatchId())).collect(Collectors.toList());
  }

  public Set<RoadId> getLocalRoadIds() {
    return localPatchIds.stream()
        .map(knownPatches::get)
        .flatMap(patch -> patch.getRoadIds().stream())
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
  public void migratePatchToNeighbour(Patch patch, MapFragmentId neighbourId, TicketService ticketService) {
    // log.info("migrate to nieghbours patch {} id {}", patch.getPatchId().getValue(), neighbourId.getId());
    // remove from local patches
    localPatchIds.remove(patch.getPatchId());

    //remove from border patches
    mapFragmentIdToBorderPatchIds.values().forEach(patches -> patches.remove(patch.getPatchId()));

    // add new patches into border patches
    List<PatchId> newBorderPatchesAfterTransfer =
        PatchConnectionSearchUtil.findNeighbouringPatches(patch.getPatchId(), this);
    mapFragmentIdToBorderPatchIds.get(neighbourId).addAll(newBorderPatchesAfterTransfer);

    //add migrated patch into shadow patches
    boolean isEndPatch = PatchConnectionSearchUtil.isEndPatch(patch, mapFragmentId, this);
    if (!isEndPatch) {
      mapFragmentIdToShadowPatchIds.get(neighbourId).add(patch.getPatchId());
    }

    //remove shadow patches - patch should be removed from shadow patches when no neighbors are adjacent to localPatches
    List<PatchId> shadowPatchesToRemove =
        PatchConnectionSearchUtil.findShadowPatchesNeighbouringOnlyWithPatch(patch.getPatchId(), this);

    shadowPatchesToRemove.forEach(this::removePatch);
    mapFragmentIdToShadowPatchIds.forEach((key, value) -> shadowPatchesToRemove.forEach(value::remove));

    if (isEndPatch) {
      log.debug("Patch is an end patch - will be fully removed from know patches");
      removePatch(patch.getPatchId());
    }
    removeEmptyNeighbours(ticketService);
  }

  private void removePatch(PatchId id) {
    Patch removedPatch = knownPatches.remove(id);
    mapFragmentIdToShadowPatchIds.values().forEach(set -> set.remove(id));
    localPatchIds.remove(id);

    removedPatch.getRoadIds().forEach(roadIdToPatchId::remove);
    removedPatch.getLaneIds().forEach(laneIdToPatchId::remove);
    removedPatch.getJunctionIds().forEach(junctionIdToPatchId::remove);
    log.debug("Known patches after remove {}",
        knownPatches.entrySet().stream().map(a -> a.getKey().getValue() + ":" + a.getValue().getPatchId().getValue()));
  }

  private void removeEmptyNeighbours(TicketService ticketService) {
    List<MapFragmentId> lostConnectNeighbours = mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .filter(i -> i.getValue().isEmpty())
        .map(Entry::getKey)
        .toList();
    this.removedNeighbours.addAll(lostConnectNeighbours);

    lostConnectNeighbours.forEach(n -> {
      mapFragmentIdToBorderPatchIds.remove(n);
      if (ConfigurationService.getConfiguration().isTicketActive()) {
        ticketService.removeTalker(n);
      }
      if (mapFragmentIdToShadowPatchIds.containsKey(n) && mapFragmentIdToShadowPatchIds.get(n).size() == 0) {
        // log.info("Remove shadow too");
        mapFragmentIdToShadowPatchIds.remove(n);
      }
    });
  }

  public void migratePatchToMe(PatchId patchId, MapFragmentId neighbourId, MapRepository mapRepository,
      List<ImmutablePair<PatchId, MapFragmentId>> neighbourPatchIdsWithMapFragmentId, TicketService ticketService) {

    neighbourPatchIdsWithMapFragmentId = neighbourPatchIdsWithMapFragmentId.stream().map(id -> {
      MapFragmentId pairPatchId = getMapFragmentIdByPatchId(id.getLeft());
      if (pairPatchId != null) {
        return new ImmutablePair<>(id.getLeft(), pairPatchId);
      } else {
        return id;
      }
    }).collect(Collectors.toList());

    Patch patch = knownPatches.get(patchId);
    if (patch == null) {
      patch = addPatch(mapRepository, patchId);
    }
    // add to local patches
    localPatchIds.add(patch.getPatchId());

    mapFragmentIdToBorderPatchIds.computeIfAbsent(neighbourId, k -> new HashSet<>());
    boolean isLastConnectionPatch = PatchConnectionSearchUtil.isEndPatch(patch, neighbourId, this);

    if (!isLastConnectionPatch) {
      mapFragmentIdToBorderPatchIds.get(neighbourId).add(patchId);
      log.debug("Received patch {} is new border patch with sender worker", patchId.getValue());
    }

    // remove patches from border that have become internal after migration
    List<PatchId> internalPatches =
        patch.getNeighboringPatches().stream().filter(localPatchIds::contains).filter(candidatePatchId -> {
          Patch checkingNeighbour = knownPatches.get(candidatePatchId);
          return checkingNeighbour.getNeighboringPatches()
              .stream()
              .noneMatch(id -> neighbourId.equals(getMapFragmentIdByPatchId(id)));
        }).toList();

    internalPatches.forEach(id -> {
      mapFragmentIdToBorderPatchIds.get(neighbourId).remove(id);
    });

    mapFragmentIdToShadowPatchIds.values().forEach(set -> set.remove(patchId));

    //add shadow patches - patch should be added to shadow patches
    List<ImmutablePair<PatchId, MapFragmentId>> shadowPatchesToAdd = neighbourPatchIdsWithMapFragmentId.stream()
        .filter(p -> !p.getRight().equals(mapFragmentId))
        .filter(p -> !localPatchIds.contains(p.getLeft()))
        .toList();

    shadowPatchesToAdd.forEach(p -> {
      addPatch(mapRepository, p.getLeft());
    });
    shadowPatchesToAdd.forEach(pair -> {
      mapFragmentIdToShadowPatchIds.computeIfAbsent(pair.getRight(), k -> {
        if (ConfigurationService.getConfiguration().isTicketActive()) {
          ticketService.addNewTalker(k);
        }
        return new HashSet<>();
      });
      // log.info("Add shadow patch {} to neighbour {}", pair.getLeft().getValue(), pair.getRight().getId());
      mapFragmentIdToShadowPatchIds.values().forEach(p -> p.remove(pair.getLeft()));
      mapFragmentIdToShadowPatchIds.get(pair.getRight()).add(pair.getLeft());

      mapFragmentIdToBorderPatchIds.computeIfAbsent(pair.getRight(), k -> new HashSet<>());
      List<PatchId> newBorderPatches = PatchConnectionSearchUtil.findNeighbouringPatches(pair.getLeft(), this);
      mapFragmentIdToBorderPatchIds.get(pair.getRight()).addAll(newBorderPatches);
    });
    removeEmptyNeighbours(ticketService);
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

    for (Map.Entry<PatchId, MapFragmentId> entry : sentPatchesIdToMapFragmentId.entrySet()) {
      if (entry.getKey().equals(patchId)) {
        return entry.getValue();
      }
    }
    return null; // todo refactor
  }

  private Patch addPatch(MapRepository mapRepository, PatchId patchId) {
    if (knownPatches.containsKey(patchId)) {
      return knownPatches.get(patchId);
    }
    Patch addedPatch = mapRepository.getPatch(patchId);
    knownPatches.put(addedPatch.getPatchId(), addedPatch);

    addedPatch.getRoadIds().forEach(roadId -> roadIdToPatchId.put(roadId, addedPatch.getPatchId()));
    addedPatch.getLaneIds().forEach(laneId -> laneIdToPatchId.put(laneId, addedPatch.getPatchId()));
    addedPatch.getJunctionIds().forEach(junctionId -> junctionIdToPatchId.put(junctionId, addedPatch.getPatchId()));
    return addedPatch;
  }

  @Override
  public void migratePatchBetweenNeighbour(PatchId patchId, MapFragmentId destination, MapFragmentId source,
      TicketService ticketService) {
    log.debug("handle migration patch between neighbours {} -> {}, {}", source.getId(), destination.getId(),
        patchId.getValue());
    if (!knownPatches.containsKey(patchId)) {
      log.debug("There is no patch {} in known patches", patchId.getValue());
      return;
    }

    // change patches between neighbour
    // if (mapFragmentIdToShadowPatchIds.get(source) != null) {
    //   mapFragmentIdToShadowPatchIds.get(source).remove(patchId);
    // }

    mapFragmentIdToShadowPatchIds.values().forEach(set -> set.remove(patchId));

    mapFragmentIdToShadowPatchIds.computeIfAbsent(destination, k -> {
      if (ConfigurationService.getConfiguration().isTicketActive()) {
        ticketService.addNewTalker(k);
      }
      return new HashSet<>();
    });
    mapFragmentIdToShadowPatchIds.get(destination).add(patchId);

    // add new border patches into destination
    List<PatchId> localPatchesConnectedWith = PatchConnectionSearchUtil.findNeighbouringPatches(patchId, this);
    mapFragmentIdToBorderPatchIds.computeIfAbsent(destination, k -> new HashSet<>());
    mapFragmentIdToBorderPatchIds.get(destination).addAll(localPatchesConnectedWith);

    // remove border patches from source when not connected with neighbour
    if (mapFragmentIdToShadowPatchIds.get(source) == null) {
      return;
    }

    Set<PatchId> allPatchesNeighbouringWithShadowPatches = mapFragmentIdToShadowPatchIds.get(source)
        .stream()
        .filter(a -> !a.equals(patchId))
        .map(knownPatches::get)
        .flatMap(p -> p.getNeighboringPatches().stream())
        .collect(Collectors.toSet());

    final Set<PatchId> newBorderPatchesSet =
        allPatchesNeighbouringWithShadowPatches.stream().filter(this::isLocalPatch).collect(Collectors.toSet());

    mapFragmentIdToBorderPatchIds.put(source, newBorderPatchesSet);
    removeEmptyNeighbours(ticketService);
  }

  @Override
  public boolean isLocalPatch(PatchId patchId) {
    return localPatchIds.contains(patchId);
  }

  @Override
  public Patch getPatchById(PatchId patchId) {
    return knownPatches.get(patchId);
  }

  public List<MapFragmentId> getNeighboursToRemove() {
    List<MapFragmentId> lostConnectNeighbours = List.copyOf(removedNeighbours);
    removedNeighbours.clear();
    return lostConnectNeighbours;
  }

  @Override
  public MapFragmentId getMe() {
    return mapFragmentId;
  }

  @Override
  public PatchId getPatchIdByRoadId(RoadId roadId) {
    return roadIdToPatchId.get(roadId);
  }

  @Override
  public PatchId getPatchIdByLaneId(LaneId laneId) {return laneIdToPatchId.get(laneId);}

  @Override
  public void printStaistic() {
    // log.info("Local patches {}, borderPatches {} ", localPatchIds.size(),
    //     mapFragmentIdToBorderPatchIds.values().stream().map(Set::size).reduce(0, Integer::sum));
  }

  @Override
  public int getLocalPatchesSize() {
    return localPatchIds.size();
  }

  @Override
  public void updateMapOfSentPatches(PatchId patchReceiver, MapFragmentId mapReceiver) {
    sentPatchesIdToMapFragmentId.put(patchReceiver, mapReceiver);
  }

  @Override
  public synchronized void clearMapOfSentPatches() {
    sentPatchesIdToMapFragmentId.clear();

  }

  @Override
  public synchronized Map<PatchId, MapFragmentId> getMapOfSentPatches() {
    return sentPatchesIdToMapFragmentId;

  }

  public void printFullStatistic() {
    String localPatch = localPatchIds.stream().map(PatchId::getValue).collect(Collectors.joining(","));

    String borderPatches = mapFragmentIdToBorderPatchIds.entrySet()
        .stream()
        .map(k -> k.getKey().getId() + " -> " + k.getValue()
            .stream()
            .map(PatchId::getValue)
            .sorted()
            .collect(Collectors.joining(",")))
        .collect(Collectors.joining("\n"));

    String shadowPatches = mapFragmentIdToShadowPatchIds.entrySet()
        .stream().map(k -> k.getKey().getId() + " -> " + k.getValue().stream().map(PatchId::getValue)
            .sorted()
            .collect(Collectors.joining(",")))
        .collect(Collectors.joining("\n"));

    String response =
        "\n************" + mapFragmentId.getId() + "****************\n" + "local: " + localPatch + "\n" + "border: \n"
            + borderPatches + "\n" + "shadow: \n" + shadowPatches + "\n"
            + "*********************************************";
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
        .localPatchesIds(localPatchIds.stream().map(PatchId::getValue).toList())
        .step(step)
        .build();
  }

  @Override
  public Set<PatchReader> getShadowPatchesReadable() {
    return knownPatches.values()
        .stream()
        .filter(patch -> !localPatchIds.contains(patch.getPatchId()))
        .map(patch -> (PatchReader) patch)
        .collect(Collectors.toSet());
  }

  public static final class MapFragmentBuilder {

    private final MapFragmentId mapFragmentId;
    private final Map<PatchId, Patch> knownPatches = new ConcurrentHashMap<>();
    private final Set<PatchId> localPatchIds = new HashSet<>();
    private final Map<MapFragmentId, Set<PatchId>> shadowPatches = new HashMap<>();
    private final Map<PatchId, MapFragmentId> shadowPatchOwnership = new HashMap<>();
    private final Map<PatchId, MapFragmentId> sentPatchesIdToMapFragmentId = new ConcurrentHashMap<>();

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

      Map<RoadId, PatchId> roadToPatch = knownPatches.values()
          .stream()
          .map(patch -> patch.getRoadIds()
              .stream()
              .collect(Collectors.toMap(Function.identity(), roadId -> patch.getPatchId())))
          .flatMap(map -> map.entrySet().stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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

      return new MapFragment(mapFragmentId, knownPatches, localPatchIds, borderPatches, shadowPatches, roadToPatch,
          laneToPatch, junctionToPatch, sentPatchesIdToMapFragmentId, new LinkedList<>());
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
