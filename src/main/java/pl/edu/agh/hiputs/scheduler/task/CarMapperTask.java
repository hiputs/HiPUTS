package pl.edu.agh.hiputs.scheduler.task;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

@Slf4j
@RequiredArgsConstructor
public class CarMapperTask implements Runnable {

  /**
   * Patch to serialized
   */
  private final Patch patch;

  /**
   * List to save collection after serialized
   */
  private final List<SerializedCar> serializedCars;

  @Override
  public void run() {
    try {
      serializedCars.addAll(patch.streamRoadsEditable()
          .flatMap(RoadEditable::pollIncomingCars)
          .map(SerializedCar::new)
          .toList());
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
