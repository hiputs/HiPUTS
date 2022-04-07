package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.Map;
import java.util.Set;

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
}
