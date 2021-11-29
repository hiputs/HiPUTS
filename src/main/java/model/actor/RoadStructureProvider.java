package model.actor;

import model.id.JunctionId;
import model.id.LaneId;
import model.map.Junction;
import model.map.LaneReadOnly;

public interface RoadStructureProvider {

    LaneReadOnly getLane(LaneId laneId);

    Junction getJunction(JunctionId junctionId);

}
