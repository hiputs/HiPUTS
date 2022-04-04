package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionRead;
import pl.edu.agh.hiputs.model.map.LaneRead;

public interface RoadStructureReader {

    LaneRead getLaneReadById(LaneId laneId);

    JunctionRead getJunctionReadById(JunctionId junctionId);

}
