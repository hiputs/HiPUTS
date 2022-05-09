package pl.edu.agh.hiputs.startingUp;

import static java.lang.Thread.sleep;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
import pl.edu.agh.hiputs.visualization.graphstream.TrivialGraphBasedVisualizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerStrategyService implements Strategy, Runnable, Subscriber {

  private final SubscriptionService subscriptionService;
  private final MapRepository mapRepository;
  private final MapFragmentExecutor mapFragmentExecutor;
  private final ConfigurationService configurationService;
  private TrivialGraphBasedVisualizer graphBasedVisualizer;

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.RunSimulationMessage);
  }

  @Override
  public void run() {
    try {

      Configuration configuration = configurationService.getConfiguration();

      if (configuration.isEnableGUI()) {
        enabledGUI();
      }

      mapRepository.readMapAndBuildModel();

      while (true) {
        mapFragmentExecutor.run();
        graphBasedVisualizer.redrawCars();
        sleep(200);
      }
    } catch (InterruptedException e) {
      log.error("Worker fail", e);
    }
  }

  private void enabledGUI() throws InterruptedException {
    log.info("Start work in single mode");
    graphBasedVisualizer = new TrivialGraphBasedVisualizer(mapFragmentExecutor.mapFragment);

    graphBasedVisualizer.showGui();
    sleep(1000);
  }

  @Override
  public void notify(Message message) {

  }
}
