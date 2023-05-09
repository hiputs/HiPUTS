package pl.edu.agh.hiputs.startingUp;

import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.RunSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ServerInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ShutDownMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
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
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.loadbalancer.MonitorLocalService;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.server.StatisticSummaryService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
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
  private final ConfigurationService configurationService;
  private TrivialGraphBasedVisualizer graphBasedVisualizer;
  private final MessageSenderService messageSenderService;
  private final MessageReceiverService messageReceiverService;
  private Configuration configuration;
  private final MapFragmentCreator mapFragmentCreator;
  private final SimulationStatisticService simulationStatisticService;

  private final ExecutorService simulationExecutor = newSingleThreadExecutor();
  private final MapFragmentId mapFragmentId = MapFragmentId.random();

  private final MonitorLocalService monitorLocalService;

  private final StatisticSummaryService statisticSummaryService;

  private final DebugUtils debugUtils;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, RunSimulationMessage);
    subscriptionService.subscribe(this, ServerInitializationMessage);
    subscriptionService.subscribe(this, ShutDownMessage);
  }

  @Override
  public void executeStrategy() {
    try {
      configuration = configurationService.getConfiguration();
      configuration.setMapFragmentId(mapFragmentId);
      messageSenderService.sendServerMessage(
          new WorkerConnectionMessage("127.0.0.1", messageReceiverService.getPort(), mapFragmentId.getId()));

      mapRepository.readMapAndBuildModel();
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
      default -> log.warn("Unhandled message " + message.getMessageType());
    }
  }

  @Autowired
  private ApplicationContext context;
  private void shutDown() {
    int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
    System.exit(exitCode);
  }

  private void handleInitializationMessage(
      pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage message) {
    waitForMapLoad();
    MapFragment mapFragment = mapFragmentCreator.fromMessage(message, mapFragmentId);
    mapFragmentExecutor.setMapFragment(mapFragment);
    debugUtils.setMapFragment(mapFragment);

    createCars();

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
  }

  private void waitForMapLoad() {
    while (!mapRepository.isReady()) {
      try {
        sleep(100); //active waiting for load map from disk
      } catch (InterruptedException e) {
        log.warn("Error util waiting for map will be load", e);
      }
    }
  }

  private void createCars() {
    final ExampleCarProvider exampleCarProvider =
        new ExampleCarProvider(mapFragmentExecutor.getMapFragment(), mapRepository);
    //for test reduce number of cars to 1
    mapFragmentExecutor.getMapFragment().getLocalLaneIds().stream().toList().subList(0,1).forEach(laneId -> {
      List<Car> generatedCars = IntStream.range(0, configuration.getInitialNumberOfCarsPerLane())
          .mapToObj(x -> exampleCarProvider.generateCar(laneId, 30))
          .filter(Objects::nonNull)
          .sorted(Comparator.comparing(Car::getPositionOnLane))
          .collect(Collectors.toList());
      Collections.reverse(generatedCars);
      generatedCars.forEach(car -> {
        LaneEditable lane = mapFragmentExecutor.getMapFragment().getLaneEditable(car.getLaneId());
        exampleCarProvider.limitSpeedPreventCollisionOnStart(car, lane);
        lane.addNewCar(car);
      });
    });
  }

  private void runSimulation() {
    simulationExecutor.submit(this);
  }

  @Override
  public void run() {
    int i = 0;
    try {
      int n = configuration.getSimulationStep();
      monitorLocalService.init(mapFragmentExecutor.getMapFragment());
      statisticSummaryService.startTiming();
      for (i = 0; i < n; i++) {
        log.info("Start iteration no. {}/{}", i+1, n);
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
        simulationStatisticService.sendStatistic(mapFragmentId);
      } catch (IOException e) {
        log.error("Error with send finish simulation message", e);
      }
    }
  }
}
