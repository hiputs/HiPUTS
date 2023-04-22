package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.List;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

public interface RoadStructureEditor extends RoadStructureReader {

  RoadEditable getRoadEditable(RoadId roadId);

  JunctionEditable getJunctionEditable(JunctionId junctionId);

  List<RoadEditable> getRandomRoadsEditable(int count);

}
