package pl.edu.agh.hiputs.startingUp;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategySelectionService {

  private static final String SERVER_LOCK = "serverLock.txt";

  private final SingleWorkStrategyService singleWorkStrategyService;
  private final WorkerStrategyService workerStrategyService;
  private final ServerStrategyService serverStrategyService;
  private final ConfigurationService configurationService;

  @EventListener(ApplicationReadyEvent.class)
  public void selectModeAndStartSimulation() {
    System.setProperty("java.awt.headless", "false");
    Configuration configuration = configurationService.getConfiguration();

    try {
      if (configuration.isTestMode()) {
        singleWorkStrategyService.executeStrategy();
      } else if(!isServerRunning()){
        serverStrategyService.executeStrategy();
      } else {
        workerStrategyService.executeStrategy();
      }

    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  private boolean isServerRunning() throws IOException {
    File serverLock = new File(SERVER_LOCK);
    if(!serverLock.exists()){
      serverLock.createNewFile();
      configurationService.getConfiguration().setServerOnThisMachine(true);
      return false;
    }
    return true;
  }
}
