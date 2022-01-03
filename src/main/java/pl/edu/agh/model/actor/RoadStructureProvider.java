package pl.edu.agh.model.actor;

import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.LaneReadOnly;

public interface RoadStructureProvider {

    LaneReadOnly getLane(LaneId laneId);

    Junction getJunction(JunctionId junctionId);

}
