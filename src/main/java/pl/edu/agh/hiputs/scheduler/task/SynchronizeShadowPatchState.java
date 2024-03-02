package pl.edu.agh.hiputs.scheduler.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;

@Slf4j
@RequiredArgsConstructor
public class SynchronizeShadowPatchState implements Runnable {

  private final String patchId;

  private final List<byte[]> serializedLanes;

  private final TransferDataHandler transferDataHandler;

  @Override
  public void run() {
    try {
      Patch shadowPatch = (Patch) transferDataHandler.getShadowPatchEditableCopy(new PatchId(patchId));

      if (shadowPatch == null) {
        return;
      }

      Map<String, List<Car>> newCarsOnLanes = serializedLanes
              .parallelStream()
          .map(s -> (SerializedLane) SerializationUtils.deserialize(s))
          .collect(Collectors.toMap(SerializedLane::getLaneId, SerializedLane::toRealObject));

      shadowPatch.parallelStreamLanesEditable().forEach(laneEditable -> {
        List<Car> newCarsOnLane = newCarsOnLanes.get(laneEditable.getLaneId().getValue());
        laneEditable.streamCarsFromExitEditable().toList().forEach(laneEditable::removeCar);

        if (newCarsOnLane == null) {
              return;
            }

        Collections.reverse(newCarsOnLane);
        newCarsOnLane.forEach(laneEditable::addCarAtEntry);
      });

      transferDataHandler.acceptShadowPatches(Set.of(shadowPatch));
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
