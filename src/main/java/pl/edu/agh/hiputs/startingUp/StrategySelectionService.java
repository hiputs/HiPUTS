package pl.edu.agh.hiputs.startingUp;

import java.io.File;
import java.io.IOException;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.HiPUTS;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategySelectionService {

  public static final String SERVER_LOCK = "serverLock.txt";

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
      } else if (canWorkAsServer() && !isServerRunning()) {
        serverStrategyService.executeStrategy();
      } else {
        workerStrategyService.executeStrategy();
      }

    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  private boolean canWorkAsServer() {
    if(HiPUTS.globalInitArgs.size() < 2){
      return true;
    }

    return HiPUTS.globalInitArgs.get(1).equals("SERVER");
  }

  private boolean isServerRunning() throws IOException {
    File serverLock = new File(SERVER_LOCK);
    if (serverLock.createNewFile()) {
      configurationService.getConfiguration().setServerOnThisMachine(true);
      return false;
    }
    return true;
  }

  @PreDestroy
  public void onExit() {
    if (configurationService.getConfiguration().isServerOnThisMachine()) {
      File serverLock = new File(SERVER_LOCK);
      serverLock.deleteOnExit();
    }
  }
}
