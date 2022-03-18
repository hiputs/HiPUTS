package pl.edu.agh.model.map;

import pl.edu.agh.model.id.LaneId;

import java.util.Set;

public interface JunctionRead {
    Set<LaneId> getOutgoingLanesIds();
}
