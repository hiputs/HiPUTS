package pl.edu.agh.hiputs.startingUp;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.CompletedInitializationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
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
  private final MessageSenderService messageSenderService;
  private final MessageReceiverService messageReceiverService;
  private Configuration configuration;

  private final ExecutorService simulationExecutor = newSingleThreadExecutor();

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.RunSimulationMessage);
  }

  @Override
  public void executeStrategy() {
    try {
      configuration = configurationService.getConfiguration();
      messageSenderService.sendServerMessage(new WorkerConnectionMessage("127.0.0.1", messageReceiverService.getPort(), mapFragmentExecutor.mapFragment.getMapFragmentId().getId()));

      if (configuration.isEnableGUI()) {
        enabledGUI();
      }

      mapRepository.readMapAndBuildModel();
      messageSenderService.sendServerMessage(new CompletedInitializationMessage());

    } catch (Exception e) {
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
    runSimulation();
  }

  private void runSimulation() {
    simulationExecutor.submit(this);
  }

  @Override
  public void run() {
    try {
      long n = configuration.getSimulationStep();
      for(long i=0; i< n; i++) {
        mapFragmentExecutor.run();

        if (configuration.isEnableGUI()) {
          graphBasedVisualizer.redrawCars();
          sleep(200);
        }
      }
    } catch (Exception e){
      log.error("Worker start simulation fail", e);
    }
  }
}
