package pl.edu.agh.hiputs.scheduler.task;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

@Slf4j
@RequiredArgsConstructor
public class LaneSerializationTask implements Callable<Pair<LaneId, byte[]>> {

  private final LaneEditable lane;

  @Override
  public Pair<LaneId, byte[]> call() {
    try {

      return new ImmutablePair<>(lane.getLaneId(), SerializationUtils.serialize(new SerializedLane(lane)));

    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
      return new ImmutablePair<>(lane.getLaneId(), null);
    }
  }
}
