package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionEditable;
import pl.edu.agh.hiputs.model.map.LaneEditable;

public interface RoadStructureEditor extends RoadStructureReader {

    LaneEditable getLaneEditable(LaneId laneId);

    JunctionEditable getJunctionEditable(JunctionId junctionId);

}
