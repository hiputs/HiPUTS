package pl.edu.agh.hiputs.scheduler.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@RequiredArgsConstructor
public class CarMapperTask implements Runnable {

  /**
   * Patch to serialized
   */
  private final Patch patch;

  /**
   * List to save collection after serialized
   */
  private final List<SCar> serializedCars;

  @Override
  public void run() {
    serializedCars.addAll(patch.streamLanesEditable()
        .flatMap(LaneEditable::pollIncomingCars)
        .map(SCar::new)
        .toList());
  }
}
