package pl.edu.agh.hiputs.model.map.mapfragment;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

public interface RoadStructureEditor extends RoadStructureReader {

    LaneEditable getLaneEditable(LaneId laneId);

    JunctionEditable getJunctionEditable(JunctionId junctionId);

}
