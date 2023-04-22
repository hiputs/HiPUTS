package pl.edu.agh.hiputs.model.map.mapfragment;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

public interface RoadStructureReader {

  RoadReadable getRoadReadable(RoadId roadId);

  JunctionReadable getJunctionReadable(JunctionId junctionId);

}
