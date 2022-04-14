package pl.edu.agh.hiputs.model.map.mapfragment;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

public interface RoadStructureReader {

  LaneReadable getLaneReadable(LaneId laneId);

  JunctionReadable getJunctionReadable(JunctionId junctionId);

}
