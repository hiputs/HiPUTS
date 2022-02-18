package pl.edu.agh.model.map;

import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Set;

public interface PatchReadWrite extends PatchRead {

    LaneReadWrite getLaneReadWriteById(LaneId laneId);

    JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId);

}
