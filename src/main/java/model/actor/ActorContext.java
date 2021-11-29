package model.actor;

import model.car.Car;
import model.id.ActorId;
import model.id.JunctionId;
import model.id.LaneId;
import model.id.PatchId;
import model.map.Junction;
import model.map.LaneReadOnly;
import model.map.Patch;

import java.util.Map;
import java.util.Set;

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

    private void stage(Car car) {
        throw new UnsupportedOperationException("method not implemented!");
        // Add car to some collection for future sending
    }

}
