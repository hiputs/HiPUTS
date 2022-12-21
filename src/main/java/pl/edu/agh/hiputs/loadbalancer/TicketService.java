package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.model.id.MapFragmentId;

public interface TicketService {

  void setActualStep(int step);

  MapFragmentId getActualTalker();

  void addNewTalker(MapFragmentId neighbourId);

  void removeTalker(MapFragmentId neighbourId);
}
