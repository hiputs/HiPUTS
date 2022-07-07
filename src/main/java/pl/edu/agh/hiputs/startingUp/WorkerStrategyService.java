package pl.edu.agh.hiputs.startingUp;

import static java.lang.Math.abs;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.RunSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ServerInitializationMessage;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.CompletedInitializationMessage;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.WorkerConnectionMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
import pl.edu.agh.hiputs.utils.MapFragmentCreator;
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
  private final MapFragmentCreator mapFragmentCreator;

  private final ExecutorService simulationExecutor = newSingleThreadExecutor();
  private final MapFragmentId mapFragmentId = MapFragmentId.random();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, RunSimulationMessage);
    subscriptionService.subscribe(this, ServerInitializationMessage);
  }

  @Override
  public void executeStrategy() {
    try {
      configuration = configurationService.getConfiguration();
      messageSenderService.sendServerMessage(
          new WorkerConnectionMessage("127.0.0.1", messageReceiverService.getPort(), mapFragmentId.getId()));

      mapRepository.readMapAndBuildModel();
    } catch (Exception e) {
      log.error("Worker fail", e);
    }
  }

  private void enabledGUI() throws InterruptedException {
    log.info("Start work in single mode");
    graphBasedVisualizer = new TrivialGraphBasedVisualizer(mapFragmentExecutor.getMapFragment());

    graphBasedVisualizer.showGui();
    sleep(1000);
  }

  @Override
  public void notify(Message message) {
    switch (message.getMessageType()) {
      case RunSimulationMessage -> runSimulation();
      case ServerInitializationMessage -> handleInitializationMessage(
          (pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage) message);
      default -> log.warn("Unhandled message " + message.getMessageType());
    }
  }

  private void handleInitializationMessage(
      pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage message) {
    waitForMapLoad();
    MapFragment mapFragment = mapFragmentCreator.fromMessage(message, mapFragmentId);
    mapFragmentExecutor.setMapFragment(mapFragment);
    // createCar();

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

  private void createCar() { //fixMe impl real car provider
    final ExampleCarProvider exampleCarProvider = new ExampleCarProvider(mapFragmentExecutor.getMapFragment());
    Random random = new Random();
    mapFragmentExecutor.getMapFragment().getLocalLaneIds().forEach(laneId -> {
      LaneEditable lane = mapFragmentExecutor.getMapFragment().getLaneEditable(laneId);
      int randomCarCount = abs(random.nextInt() % 3);
      for (int i = 0; i < randomCarCount; i++) {
        double carPosition = (randomCarCount - i) * lane.getLength() / (randomCarCount + 1);
        Car car = exampleCarProvider.generateCar(carPosition);
        lane.addCarAtEntry(car);
      }
    });
  }

  private void runSimulation() {
    simulationExecutor.submit(this);
  }

  @Override
  public void run() {
    try {
      long n = configuration.getSimulationStep();
      for (long i = 0; i < n; i++) {
        mapFragmentExecutor.run();

        if (configuration.isEnableGUI()) {
          graphBasedVisualizer.redrawCars();
          sleep(200);
        }
      }
    } catch (Exception e) {
      log.error("Worker start simulation fail", e);
    } finally {
      try {
        log.info("Worker finish simulation");
        messageSenderService.sendServerMessage(new FinishSimulationMessage(mapFragmentId.getId()));
      } catch (IOException e) {
        log.error("Error with send finish simulation message", e);
      }
    }
  }
}
