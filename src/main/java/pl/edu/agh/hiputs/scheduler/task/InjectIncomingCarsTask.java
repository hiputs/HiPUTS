package pl.edu.agh.hiputs.scheduler.task;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

@Slf4j
@RequiredArgsConstructor
public class InjectIncomingCarsTask implements Runnable {

  private final List<SCar> sCars;
  private final TransferDataHandler transferDataHandler;

  @Override
  public void run() {
    try {
      transferDataHandler.acceptIncomingCars(
          sCars.parallelStream()
              .map(SCar::toRealObject)
              .collect(Collectors.toSet()));
    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
