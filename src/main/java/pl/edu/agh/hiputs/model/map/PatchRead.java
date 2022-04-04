package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.Set;

public interface PatchRead {

    LaneRead getLaneReadById(LaneId laneId);

    JunctionRead getJunctionReadById(JunctionId junctionId);

    Set<LaneId> getLaneIds();

    Set<JunctionId> getJunctionIds();
}
