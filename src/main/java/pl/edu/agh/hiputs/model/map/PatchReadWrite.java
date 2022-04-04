package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

public interface PatchReadWrite extends PatchRead {

    LaneReadWrite getLaneReadWriteById(LaneId laneId);

    JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId);

}
