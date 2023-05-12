package pl.edu.agh.hiputs.service.worker;

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
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchEditor;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.CarGenerationTask;
import pl.edu.agh.hiputs.scheduler.task.CarRouteExtendTask;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarGeneratorService implements Subscriber {

  @Setter
  private MapFragment mapFragment;
  private ExampleCarProvider carProvider;
  private Configuration configuration;
  private final TaskExecutorService taskExecutor;
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private boolean bigWorker = false;
  private int step = 0;
  private int totalPatch = -1;
  private static final int START_ADD_CAR = 5;


  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    configuration = ConfigurationService.getConfiguration();
  }

  public void generateInitialCars(){
    createCarProvider();

    List<Runnable> generationTasks =
        configuration.getNumberOfCarsPerWorker() > 0 ? generateCarsDistributedEvenlyBetweenPatches()
            : generateCarsOnEachLane();

    taskExecutor.executeBatch(generationTasks);
  }

  private void putCarsOnLanes(List<Car> generatedCars) {
    generatedCars.sort(Comparator.comparing(Car::getPositionOnLane));
    Collections.reverse(generatedCars);
    // it's important to sort and reverse cars because now we place cars from the end of the road
    // to the beginning and
    // when needed car's position is modified to not be generated on other car

    generatedCars.forEach(car -> {
      LaneEditable lane = mapFragment.getLaneEditable(car.getLaneId());
      carProvider.limitSpeedPreventCollisionOnStart(car, lane);
      lane.addNewCar(car);
    });
  }

  private List<Runnable> generateCarsDistributedEvenlyBetweenPatches() {
    int minCarsPerPatch = configuration.getNumberOfCarsPerWorker() / mapFragment.getMyPatchCount();
    List<Runnable> tasks = new LinkedList<>();
    int patchIdx = 0;

    for (PatchEditor patch : mapFragment.getLocalPatchesEditable()) {
      int carsToGenerateInPatch =
          patchIdx < configuration.getNumberOfCarsPerWorker() % mapFragment.getMyPatchCount() ? minCarsPerPatch + 1
              : minCarsPerPatch;
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
    return tasks;
  }

  private List<Runnable> generateCarsOnEachLane() {
    return mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> new CarGenerationTask(carProvider, mapFragment, laneId,
            configuration.getInitialNumberOfCarsPerLane()))
        .collect(Collectors.toList());
  }

  public void extendCarsRoutes(int step){
    if(configuration.isExtendCarRouteWhenItEnds()){

      List<Runnable> extendRoutes = mapFragment.getLocalLaneIds()
          .parallelStream()
          .map(laneId -> new CarRouteExtendTask(carProvider, mapFragment, laneId, configuration.getSimulationStep() - step))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(extendRoutes);
    }
  }

  public void generateCarsAfterStep(MapFragment mapFragment) {
    // Configuration configuration = configurationService.getConfiguration();

    if(totalPatch == -1){
      totalPatch = mapRepository.getAllPatches().size();
    }

    if(configuration.getNewCars() == 0){
      return;
    }

    int targetCarMax = (int)(configuration.getNewCars() / (totalPatch * 1.0)* mapFragment.getMyPatchCount());
    int targetCarMin = (int)(configuration.getMinCars() / (totalPatch * 1.0)* mapFragment.getMyPatchCount());
    if(targetCarMax <= targetCarMin){
      targetCarMax = targetCarMin + 1;
    }
    int count = ThreadLocalRandom.current().nextInt(targetCarMin, targetCarMax);

    if (step++ < START_ADD_CAR) {
      return;
    }

    if(bigWorker){
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

  private void createCarProvider(){
    if(carProvider == null) {
      try {
        carProvider = new ExampleCarProvider(mapFragment, mapRepository);
      }
      catch(NullPointerException e){
        throw new NullPointerException("MapFragment is null. "
            + "You need to provide MapFragment object before creating CarProvider object.");
      }
    }
  }

  @Override
  public void notify(Message message) {
    bigWorker = ((ServerInitializationMessage) message).isBigWorker();
  }



}
