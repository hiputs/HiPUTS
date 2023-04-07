package pl.edu.agh.hiputs.service.worker;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private final ConfigurationService configurationService;
  private boolean bigWorker = false;
  private int step = 0;
  private int totalPatch = -1;
  private static final int START_ADD_CAR = 5;


  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    configuration = configurationService.getConfiguration();
  }

  public void generateInitialCars(){
    createCarProvider();

    List<Car> generatedCars = configuration.getNumberOfCarsPerWorker() > 0 ?
        generateCarsDistributedEvenlyBetweenPatches() :
        generateCarsOnEachLane();

    putCarsOnLanes(generatedCars);
  }

  private void putCarsOnLanes(List<Car> generatedCars) {
    generatedCars.forEach(car -> {
      LaneEditable lane = mapFragment.getLaneEditable(car.getLaneId());
      carProvider.limitSpeedPreventCollisionOnStart(car, lane);
      lane.addNewCar(car);
    });
  }

  private List<Car> generateCarsDistributedEvenlyBetweenPatches() {
    int idx = 0;
    List<Car> generatedCars = new LinkedList<>();

    for(PatchEditor patch : mapFragment.getLocalPatchesEditable()){
      int carsToGenerateInPatch = idx++ < configuration.getNumberOfCarsPerWorker() % mapFragment.getMyPatchCount()
          ? configuration.getNumberOfCarsPerWorker() / mapFragment.getMyPatchCount() + 1
          : configuration.getNumberOfCarsPerWorker() / mapFragment.getMyPatchCount();

      LaneId[] lanes = patch.getLaneIds().toArray(new LaneId[0]);

      for(int i=0;i<carsToGenerateInPatch;i++){
        generatedCars.add(carProvider.generateCar(lanes[i % lanes.length]));
      }
    }

    return generatedCars;
  }

  private List<Car> generateCarsOnEachLane(){
    return mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> {
          List<Car> generatedCars = IntStream.range(0, configuration.getInitialNumberOfCarsPerLane())
              .mapToObj(x -> carProvider.generateCar(laneId))
              .filter(Objects::nonNull)
              .sorted(Comparator.comparing(Car::getPositionOnLane))
              .collect(Collectors.toList());
          Collections.reverse(generatedCars);

          return generatedCars;
        })
        .flatMap(Collection::stream)
        .toList();
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
      count = (int) (count * 10);
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
