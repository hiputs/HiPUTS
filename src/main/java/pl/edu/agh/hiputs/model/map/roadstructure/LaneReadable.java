package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.List;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

public interface LaneReadable {

  LaneId getLaneId();

  RoadId getRoadId();

  List<LaneId> getLaneSuccessors();

}
