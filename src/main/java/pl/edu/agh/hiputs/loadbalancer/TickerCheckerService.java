package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.model.id.MapFragmentId;

public interface TickerCheckerService {

  void setActualStep(int step);

  MapFragmentId getActualTalker();

}
