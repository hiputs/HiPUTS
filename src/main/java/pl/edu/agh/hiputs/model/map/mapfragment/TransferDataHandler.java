package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

public interface TransferDataHandler {

  /**
   * Returns a set of MapFragmentIds of neighboring MapFragments
   */
  Set<MapFragmentId> getNeighbors();

  /**
   * Returns and clears the contents of collections holding Cars incoming to remote Lanes.
   * The Cars should be partitioned based on the MapFragmentId of the owner of the targeted lanes.
   * The method should also ensure that the Cars will not be returned again in the subsequent invocations
   * (usually in the next iteration of the simulation).
   */
  Map<MapFragmentId, Set<CarReadable>> pollOutgoingCars();

  /**
   * Accepts Cars incoming to border Lanes.
   * This method may be invoked multiple times for a single iteration.
   */
  void acceptIncomingCars(Set<Car> incomingCars);

  /**
   * Returns the border Patches to be sent as shadow Patches to neighbors.
   * The Patches should be partitioned based on the MapFragmentId of the owner of the targeted lanes.
   */
  Map<MapFragmentId, Set<Patch>> getBorderPatches();

  /**
   * Accepts border Patches of neighbors and uses them to overwrite shadow Patches.
   * This method may be invoked multiple times for a single iteration.
   */
  void acceptShadowPatches(Set<Patch> shadowPatches);

  /**
   * Return  patches from worker's point of view
   */
  Set<PatchReader> getKnownPatchReadable();

  /**
   * Return neighbourPatches
   */
  Set<PatchReader> getShadowPatchesReadable();

  void migratePatchToNeighbour(Patch patch, MapFragmentId mapFragmentId);

  void migratePatchToMe(PatchId patchId, MapFragmentId mapFragmentId, MapRepository mapRepository, List<ImmutablePair<PatchId, MapFragmentId>> patchIdWithMapFragmentId);

  MapFragmentId getMapFragmentIdByPatchId(PatchId patchId);

  void migratePatchBetweenNeighbour(PatchId patchId, MapFragmentId source, MapFragmentId destination);

  Patch getPatchById(PatchId patchId);

  MapFragmentId getMe();
}
