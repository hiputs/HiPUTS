package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.Set;

public interface JunctionRead {
    Set<LaneId> getOutgoingLanesIds();
}
