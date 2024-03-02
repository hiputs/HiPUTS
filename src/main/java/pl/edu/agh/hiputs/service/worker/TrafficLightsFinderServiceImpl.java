package pl.edu.agh.hiputs.service.worker;

import java.util.Collection;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.worker.usecase.TrafficLightsFinderService;

@Service
public class TrafficLightsFinderServiceImpl implements TrafficLightsFinderService {

  @Override
  public Collection<JunctionId> getJunctionIds(MapFragment mapFragment) {
    return mapFragment.getLocalJunctionIds().stream()
        .filter(junctionId -> mapFragment.getJunctionReadable(junctionId).getSignalsControlCenter().isPresent())
        .toList();
  }
}
