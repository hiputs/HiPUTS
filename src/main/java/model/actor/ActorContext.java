package model.actor;

import model.car.Car;
import model.id.JunctionId;
import model.id.LaneId;
import model.id.PatchId;
import model.map.*;

import java.util.Collection;
import java.util.Map;

public class ActorContext implements LaneHandler, RoadStructureProvider {

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
    public LaneRW getLane(LaneId laneId) {
        return null; // if lane is local return RW, else R
    }

    @Override
    public Junction getJunction(JunctionId junctionId) {
        return null;
    }

    @Override
    public LaneR getRLane(LaneId laneId) {
        return null;
    }

    @Override
    public void stage(Car car) {
        // Add car to some collection to send it
    }

    private LaneRemote getRemoteLane(LaneId laneId){
        return null;
    }
}
