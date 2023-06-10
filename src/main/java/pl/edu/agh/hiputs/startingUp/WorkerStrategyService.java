package pl.edu.agh.hiputs.startingUp;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.RunSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ServerInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ShutDownMessage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.CompletedInitializationMessage;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.LocalLoadMonitorService;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.CarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.utils.DebugUtils;
import pl.edu.agh.hiputs.utils.MapFragmentCreator;
import pl.edu.agh.hiputs.visualization.graphstream.TrivialGraphBasedVisualizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerStrategyService implements Strategy, Runnable, Subscriber {

  private final WorkerSubscriptionService subscriptionService;
  private final MapRepository mapRepository;
  private final MapFragmentExecutor mapFragmentExecutor;
  private TrivialGraphBasedVisualizer graphBasedVisualizer;
  private final MessageSenderService messageSenderService;
  private final MessageReceiverService messageReceiverService;
  private Configuration configuration;
  private final MapFragmentCreator mapFragmentCreator;
  private final CarGeneratorService carGeneratorService;

  private final ExecutorService simulationExecutor = newSingleThreadExecutor();
  private final MapFragmentId mapFragmentId = MapFragmentId.random();

  private final SimulationStatisticService simulationStatisticService;

  private final DebugUtils debugUtils;
  private final LocalLoadMonitorService localLoadMonitorService;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, RunSimulationMessage);
    subscriptionService.subscribe(this, ServerInitializationMessage);
    subscriptionService.subscribe(this, ShutDownMessage);
  }

  @Override
  public void executeStrategy() {
    try {
      simulationStatisticService.startStage(SimulationPoint.WORKER_INITIALIZATION);
      configuration = ConfigurationService.getConfiguration();
      configuration.setMapFragmentId(mapFragmentId);
      messageSenderService.sendServerMessage(
          new WorkerConnectionMessage("127.0.0.1", messageReceiverService.getPort(), mapFragmentId.getId()));

      simulationStatisticService.startStage(SimulationPoint.WORKER_MAP_BUILD);
      mapRepository.readMapAndBuildModel();
      simulationStatisticService.endStage(SimulationPoint.WORKER_MAP_BUILD);
    } catch (Exception e) {
      log.error("Worker fail", e);
    }
  }

  private void enabledGUI() throws InterruptedException {
    log.info("Starting GUI");
    graphBasedVisualizer = new TrivialGraphBasedVisualizer(mapFragmentExecutor.getMapFragment(), mapRepository);

    graphBasedVisualizer.showGui();
    sleep(300);
  }

  @Override
  public void notify(Message message) {
    switch (message.getMessageType()) {
      case RunSimulationMessage -> runSimulation();
      case ServerInitializationMessage -> handleInitializationMessage(
          (pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage) message);
      case ShutDownMessage -> shutDown();
      default -> log.warn("Unhandled message {}", message.getMessageType());
    }
  }

  @Autowired
  private ApplicationContext context;
  private void shutDown() {
    int exitCode = SpringApplication.exit(context, () -> 0);
    System.exit(exitCode);
  }

  private void handleInitializationMessage(
      pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage message) {
    waitForMapLoad();
    MapFragment mapFragment = mapFragmentCreator.fromMessage(message, mapFragmentId);
    mapFragmentExecutor.setMapFragment(mapFragment);
    debugUtils.setMapFragment(mapFragment);
    carGeneratorService.setMapFragment(mapFragment);

    simulationStatisticService.startStage(SimulationPoint.WORKER_INITIAL_CAR_GENERATION);
    carGeneratorService.generateInitialCars();
    simulationStatisticService.endStage(SimulationPoint.WORKER_INITIAL_CAR_GENERATION);

    if (configuration.isEnableGUI()) {
      try {
        enabledGUI();
      } catch (InterruptedException e) {
        log.error("Error with creating gui", e);
      }
    }

    try {
      messageSenderService.sendServerMessage(new CompletedInitializationMessage());
    } catch (IOException e) {
      log.error("Fail send CompletedInitializationMessage", e);
    }
    simulationStatisticService.endStage(SimulationPoint.WORKER_INITIALIZATION);
  }

  private void waitForMapLoad() {
    while (!mapRepository.isReady()) {
      try {
        sleep(10); //active waiting for load map from disk
      } catch (InterruptedException e) {
        log.warn("Error util waiting for map will be load", e);
      }
    }
  }


  private void runSimulation() {
    simulationExecutor.submit(this);
  }

  @Override
  public void run() {
    localLoadMonitorService.init(mapFragmentExecutor.getMapFragment()); // todo refactor?
    simulationStatisticService.startStage(SimulationPoint.WORKER_SIMULATION);
    int i = 0;
    try {
      int n = configuration.getSimulationStep();

      for (i = 0; i < n; i++) {
        log.info("Start iteration no. {}/{}", i + 1, n);
        mapFragmentExecutor.run(i);

        if (configuration.isEnableGUI()) {
          graphBasedVisualizer.redrawCars();
          sleep(configuration.getPauseAfterStep());
        }
      }
    } catch (Exception e) {
      log.error(String.format("Worker simulation error in %d iteration", i), e);
    } finally {
      try {
        log.info("Worker finish simulation");
        messageSenderService.sendServerMessage(new FinishSimulationMessage(mapFragmentId.getId()));
        simulationStatisticService.endStage(SimulationPoint.WORKER_SIMULATION);
        simulationStatisticService.sendStatistic(mapFragmentId);
      } catch (IOException e) {
        log.error("Error with send finish simulation message", e);
      }
    }
  }
}
