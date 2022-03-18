package pl.edu.agh.model.actor;

import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.JunctionRead;
import pl.edu.agh.model.map.LaneRead;

public interface RoadStructureProvider {

    LaneRead getLaneReadById(LaneId laneId);

    JunctionRead getJunctionReadById(JunctionId junctionId);

}
