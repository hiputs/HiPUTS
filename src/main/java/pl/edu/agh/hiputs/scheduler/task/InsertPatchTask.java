package pl.edu.agh.hiputs.scheduler.task;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class InsertPatchTask implements Runnable {

  private final PatchId patchId;
  private final List<byte[]> serializedLanes;
  private final TransferDataHandler transferDataHandler;

  @Override
  public void run() {
    Patch insertedPatch = transferDataHandler.getPatchById(patchId);

    serializedLanes.stream().map(s -> (SerializedLane) SerializationUtils.deserialize(s)).forEach(deserializedLane -> {
      LaneEditable patchLane = insertedPatch.getLaneEditable(new LaneId(deserializedLane.getLaneId()));
      patchLane.removeAllCars();

      List<Car> cars = deserializedLane.toRealObject();
      Collections.reverse(cars);
      cars.forEach(patchLane::addCarAtEntry);
      log.trace("Patch {} Line {} cars {}", insertedPatch.getPatchId().getValue(), deserializedLane.getLaneId(),
          cars.size());
    });
    log.debug("Patch {} inserted", patchId.getValue());
  }

}
