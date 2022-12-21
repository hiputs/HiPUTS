package pl.edu.agh.hiputs.service.worker;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarGeneratorService implements Subscriber {

  private static final int START_ADD_CAR = 5;
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private final ConfigurationService configurationService;
  private boolean bigWorker = false;
  private int step = 0;
  private int totalPatch = -1;

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
  }

  public void generateCars(MapFragment mapFragment) {
    Configuration configuration = configurationService.getConfiguration();

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
    ExampleCarProvider carProvider = new ExampleCarProvider(mapFragment, mapRepository);

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

  @Override
  public void notify(Message message) {
    bigWorker = ((ServerInitializationMessage) message).isBigWorker();
  }
}
