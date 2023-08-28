package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.loadbalancer.TicketService;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchEditor;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

public interface TransferDataHandler {

  /**
   * Returns a set of MapFragmentIds of neighboring MapFragments
   */
  Set<MapFragmentId> getNeighbors();

  /**
   * Returns and clears the contents of collections holding Cars incoming to remote Roads.
   * The Cars should be partitioned based on the MapFragmentId of the owner of the destination lane of each car.
   * The method should also ensure that the Cars will not be returned again in the subsequent invocations
   * (usually in the next iteration of the simulation).
   */
  Map<MapFragmentId, Set<CarEditable>> pollOutgoingCars();

  /**
   * Accepts Cars incoming to border Roads.
   * This method may be invoked multiple times for a single iteration.
   */
  void acceptIncomingCars(Set<Car> incomingCars);

  /**
   * Returns the border Patches to be sent as shadow Patches to neighbors.
   * The Patches should be partitioned based on the MapFragmentId of the owner of the targeted roads.
   */
  Map<MapFragmentId, Set<Patch>> getBorderPatches();

  /**
   * Accepts border Patches of neighbors and uses them to overwrite shadow Patches.
   * This method may be invoked multiple times for a single iteration.
   */
  void acceptShadowPatches(Set<Patch> shadowPatches);

  /**
   * Return patches from worker's point of view
   */
  Set<PatchReader> getKnownPatchReadable();

  /**
   * Return neighbourPatches
   */
  Set<PatchReader> getShadowPatchesReadable();

  /**
   * Returns editable copy of shadow patch
   */
  PatchEditor getShadowPatchEditableCopy(PatchId patchId);

  void migratePatchToNeighbour(Patch patch, MapFragmentId mapFragmentId, TicketService ticketService);

  void migratePatchToMe(PatchId patchId, MapFragmentId mapFragmentId, MapRepository mapRepository,
      List<ImmutablePair<PatchId, MapFragmentId>> neighbourPatchIdsWithMapFragmentId, TicketService ticketService);

  MapFragmentId getMapFragmentIdByPatchId(PatchId patchId);

  void migratePatchBetweenNeighbour(PatchId patchId, MapFragmentId destination, MapFragmentId source,
      TicketService ticketService);

  Patch getPatchById(PatchId patchId);

  MapFragmentId getMe();

  /**
   * checks if patch belongs to the local set
   */
  boolean isLocalPatch(PatchId patchId);

  PatchId getPatchIdByRoadId(RoadId roadId);

  PatchId getPatchIdByLaneId(LaneId laneId);

  void printStaistic();

  int getLocalPatchesSize();

  /**
   * Map of patches sent to neighbours during Load Balancing
   *
   * @param patchReceiver
   * @param mapReceiver
   */
  void updateMapOfSentPatches(PatchId patchReceiver, MapFragmentId mapReceiver);

  void clearMapOfSentPatches();

  Map<PatchId, MapFragmentId> getMapOfSentPatches();

  List<MapFragmentId> getNeighboursToRemove();

}
