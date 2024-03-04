package pl.edu.agh.hiputs.service.routegenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.CarGenerationTask;
import pl.edu.agh.hiputs.scheduler.task.CarRouteExtendTask;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "car-generator.new-generator", havingValue = "false")
public class RandomCarGeneratorService implements Subscriber, CarGeneratorService {

  private static final int START_ADD_CAR = 5;
  private final TaskExecutorService taskExecutor;
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private final Configuration configuration;
  @Setter
  private MapFragment mapFragment;
  private ExampleCarProvider carProvider;
  private boolean bigWorker = false;
  private int totalPatch = -1;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
  }

  public void generateInitialCars(MapFragment mapFragment) {
    if (configuration.getNumberOfCarsPerWorker() <= 0 && configuration.getInitialNumberOfCarsPerLane() <= 0) {
      return; //todo refactor
    }
    setMapFragment(mapFragment);
    createCarProvider();
    List<Runnable> generationTasks =
        configuration.getNumberOfCarsPerWorker() > 0 ? generateCarsDistributedEvenlyBetweenPatches()
            : generateCarsOnEachLane();
    taskExecutor.executeBatch(generationTasks);
  }

  private List<Runnable> generateCarsDistributedEvenlyBetweenPatches() {
    long t0 = System.currentTimeMillis();
    int perWorkerCars =
        bigWorker ? configuration.getNumberOfCarsInBigWorker() : configuration.getNumberOfCarsPerWorker();
    int minCarsPerPatch = mapFragment.getMyPatchCount() > 0 ? perWorkerCars / mapFragment.getMyPatchCount() : 0;

    List<Runnable> tasks = new LinkedList<>();
    int patchIdx = 0;

    for (PatchEditor patch : mapFragment.getLocalPatchesEditable()) {
      int carsToGenerateInPatch =
          patchIdx < perWorkerCars % mapFragment.getMyPatchCount() ? minCarsPerPatch + 1 : minCarsPerPatch;
      int minCarsPerLane = carsToGenerateInPatch / patch.getLaneIds().size();

      int taskIdx = 0;
      for (LaneId lane : patch.getLaneIds()) {
        int carsToGenerateOnLane =
            taskIdx < carsToGenerateInPatch % patch.getLaneIds().size() ? minCarsPerLane + 1 : minCarsPerLane;
        tasks.add(new CarGenerationTask(carProvider, mapFragment, lane, carsToGenerateOnLane));
        taskIdx++;
      }
      patchIdx++;
    }
    long t1 = System.currentTimeMillis();
    log.debug("GENERATE CAR DIVISION TIME: {}", t1 - t0);
    return tasks;
  }

  private List<Runnable> generateCarsOnEachLane() {
    return mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> new CarGenerationTask(carProvider, mapFragment, laneId,
            configuration.getInitialNumberOfCarsPerLane()))
        .collect(Collectors.toList());
  }

  private void createCarProvider() {
    if (carProvider == null) {
      try {
        carProvider = new ExampleCarProvider(mapFragment, mapRepository);
      } catch (NullPointerException e) {
        throw new NullPointerException(
            "MapFragment is null. " + "You need to provide MapFragment object before creating CarProvider object.");
      }
    }
  }

  private void putCarsOnLanes(List<Car> generatedCars) {
    generatedCars.sort(Comparator.comparing(Car::getPositionOnLane));
    Collections.reverse(generatedCars);

    generatedCars.forEach(car -> {
      LaneEditable lane = mapFragment.getLaneEditable(car.getLaneId());
      carProvider.limitSpeedPreventCollisionOnStart(car, lane);
      lane.addNewCar(car);
    });
  }

  @Override
  public void manageCars(MapFragment mapFragment, int step) {
    if (configuration.isExtendCarRouteWhenItEnds()) {
      extendCarsRoutes(step);
    } else if (configuration.getNewCars() > 0) {
      generateCarsAfterStep(mapFragment, step);
    }
  }

  private void extendCarsRoutes(int step) {
    List<Runnable> extendRoutes = mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> new CarRouteExtendTask(carProvider, mapFragment, laneId,
            configuration.getSimulationStep() - step))
        .collect(Collectors.toList());
    taskExecutor.executeBatch(extendRoutes);
  }

  public void generateCarsAfterStep(MapFragment mapFragment, int step) {
    setMapFragment(mapFragment);
    if (totalPatch == -1) {
      totalPatch = mapRepository.getAllPatches().size();
    }

    if (configuration.getNewCars() == 0) {
      return;
    }

    int targetCarMax = (int) (configuration.getNewCars() / (totalPatch * 1.0) * mapFragment.getMyPatchCount());
    int targetCarMin = (int) (configuration.getMinCars() / (totalPatch * 1.0) * mapFragment.getMyPatchCount());
    if (targetCarMax <= targetCarMin) {
      targetCarMax = targetCarMin + 1;
    }
    int count = ThreadLocalRandom.current().nextInt(targetCarMin, targetCarMax);

    if (step++ < START_ADD_CAR) {
      return;
    }

    if (bigWorker) {
      count = count * 10;
    }

    List<LaneEditable> lanesEditable = mapFragment.getRandomLanesEditable(count);
    // ExampleCarProvider carProvider = new ExampleCarProvider(mapFragment, mapRepository);
    createCarProvider();

    final List<Car> carsCreated = lanesEditable.parallelStream().map(lane -> {
      int hops = ThreadLocalRandom.current().nextInt(20, 40);

      if (bigWorker) {
        hops = 20;
      }
      Car car = carProvider.generateCar(lane.getLaneId(), hops);

      if (car == null) {
        return null;
      }
      carProvider.limitSpeedPreventCollisionOnStart(car, lane);
      lane.addNewCar(car);
      return car;
    }).filter(Objects::nonNull).toList();

    log.info("Created cars {} with {}", carsCreated.size(), count);

  }

  public Car replaceCar(CarEditable car) {
    Car newCar = carProvider.generateCar(car.getPositionOnLane(), car.getLaneId(), car.getSpeed());
    LaneEditable lane = mapFragment.getLaneEditable(car.getLaneId());
    lane.replaceCar(car, newCar);

    return newCar;
  }

  @Override
  public void notify(Message message) {
    bigWorker = ((ServerInitializationMessage) message).isBigWorker();
  }
}
