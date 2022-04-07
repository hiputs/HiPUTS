package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionReadable;
import pl.edu.agh.hiputs.model.map.LaneReadable;

public interface RoadStructureReader {

    LaneReadable getLaneReadable(LaneId laneId);

    JunctionReadable getJunctionReadable(JunctionId junctionId);

}
