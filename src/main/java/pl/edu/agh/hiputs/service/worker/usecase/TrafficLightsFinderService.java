package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.Collection;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;

public interface TrafficLightsFinderService {

  Collection<JunctionId> getJunctionIds(MapFragment mapFragment);
}
