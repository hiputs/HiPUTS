package model.actor;

import model.id.JunctionId;
import model.id.LaneId;
import model.map.Junction;
import model.map.LaneRW;

public interface RoadStructureProvider {

    LaneRW getLane(LaneId laneId);

    Junction getJunction(JunctionId junctionId);

}
