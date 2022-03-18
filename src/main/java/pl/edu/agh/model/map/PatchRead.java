package pl.edu.agh.model.map;

import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Set;

public interface PatchRead {

    LaneRead getLaneReadById(LaneId laneId);

    JunctionRead getJunctionReadById(JunctionId junctionId);

    Set<LaneId> getLaneIds();

    Set<JunctionId> getJunctionIds();
}
