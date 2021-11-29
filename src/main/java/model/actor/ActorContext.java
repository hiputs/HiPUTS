package model.actor;

import model.car.Car;
import model.id.JunctionId;
import model.id.LaneId;
import model.id.PatchId;
import model.map.*;

import java.util.Collection;
import java.util.Map;

public class ActorContext implements LaneEditor, RoadStructureProvider {

    /**
     * All patches within this ActorContext - they represent patches that are situated within the same JVM
     */
    private Map<PatchId, Patch> localPatches;
    /**
     * All remote patches (shadow patches from neighboring Actor Contexts) to this ActorContext
     */
    private Map<PatchId, Patch> remotePatches;

    /**
     * lane to patch mapper
     */
    private Map<LaneId, Patch> lane2Patch;

    /**
     * Neighboring actorContexts
     */
    private Collection<ActorContext> neighbours;


    @Override
    public LaneReadOnly getReadableOnlyLane(LaneId laneId) {
        throw new Error("method not implemented!");
    }

    @Override
    public LaneReadWrite getLaneReadWrite(LaneId laneId) {
        throw new Error("method not implemented!");
    }

    @Override
    public Junction getJunction(JunctionId junctionId) {
        throw new Error("method not implemented!");
    }

    @Override
    public void stage(Car car) {
        throw new Error("method not implemented!");
        // Add car to some collection to send it
    }

}
