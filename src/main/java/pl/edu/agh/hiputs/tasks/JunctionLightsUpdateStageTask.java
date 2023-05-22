package pl.edu.agh.hiputs.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.worker.strategies.TrafficLightsStrategy;

@Slf4j
@RequiredArgsConstructor
public class JunctionLightsUpdateStageTask implements Runnable{

  private final MapFragment mapFragment;
  private final JunctionId junctionId;
  private final TrafficLightsStrategy strategy;

  @Override
  public void run() {
    try {
      mapFragment.getJunctionReadable(junctionId).getSignalsControlCenter().ifPresent(strategy::execute);
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
