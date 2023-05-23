package pl.edu.agh.hiputs.service.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.util.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.example.ExampleCarProvider;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.pathfinder.CHBidirectionalDijkstra;
import pl.edu.agh.hiputs.service.pathfinder.CarProvider;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarGeneratorService implements Subscriber {

  private static final int START_ADD_CAR = 0;
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private boolean bigWorker = false;
  private int step = 0;
  private int totalPatch = -1;


  ThreadPoolExecutor executors;
  private CHBidirectionalDijkstra chBidirectionalDijkstra = null;
  private final Configuration configuration = ConfigurationService.getConfiguration();
  private final Configuration.PathGeneratorConfiguration pathGeneratorConfiguration =
          configuration.getPathGeneratorConfiguration();
  private final CarProvider carProvider = new CarProvider();

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    int threadNumber = pathGeneratorConfiguration.getThreadsForExecutors();
    if (threadNumber < 1) {
      threadNumber = 1;
    }
    executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber);
  }

  public void generateCars(MapFragment mapFragment) {
    if(totalPatch == -1){
      totalPatch = mapRepository.getAllPatches().size();
    }
    if (chBidirectionalDijkstra == null) {
      if (totalPatch > 0 && mapRepository.isReady()) {
        chBidirectionalDijkstra = new CHBidirectionalDijkstra(mapRepository, executors);
      }
      else {
        chBidirectionalDijkstra = new CHBidirectionalDijkstra(mapFragment, executors);
      }
    }


    if(configuration.getNewCars() == 0){
      return;
    }
    System.out.println(configuration.getNewCars());
    System.out.println(totalPatch);
    System.out.println(mapFragment.getMyPatchCount());
    System.out.println((configuration.getNewCars() / ((totalPatch+1) * 1.0)));
    int targetCarMax = (int)(configuration.getNewCars() / ((totalPatch+1) * 1.0)* mapFragment.getMyPatchCount());
    int targetCarMin = (int)(configuration.getMinCars() / ((totalPatch+1) * 1.0)* mapFragment.getMyPatchCount());
    if(targetCarMax <= targetCarMin){
      targetCarMax = targetCarMin + 1;
    }
    int count = ThreadLocalRandom.current().nextInt(targetCarMin, targetCarMax);

    if (step++ < START_ADD_CAR) {
      return;
    }



    if(bigWorker){
      count = (count * 10);
    }

    List<Car> carsCreated;
    if (pathGeneratorConfiguration.getGeneratorType().equals("random")) {
      List<LaneEditable> lanesEditable = mapFragment.getRandomLanesEditable(count);
      ExampleCarProvider carProvider = new ExampleCarProvider(mapFragment, mapRepository);

      carsCreated = lanesEditable.parallelStream().map(lane -> {
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
    } else {
      Set<LaneId> lanes = mapFragment.getLocalLaneIds();
      LaneId[] laneIds = lanes.toArray(new LaneId[0]);

      List<LaneEditable> startLanes = new ArrayList<>();
      List<Pair<LaneId, LaneId>> requests = new ArrayList<>();
      for (int i=0; i<count; i++) {
        startLanes.add(mapFragment.getLaneEditable(laneIds[ThreadLocalRandom.current().nextInt(0, laneIds.length)]));
        requests.add(new Pair<>(
                startLanes.get(i).getLaneId(),
                mapFragment.getLaneEditable(laneIds[ThreadLocalRandom.current().nextInt(0, laneIds.length)]).getLaneId()
        ));
      }

      List<RouteWithLocation> routeWithLocationList;
      if (pathGeneratorConfiguration.getThreadsForExecutors() == 0) {
        routeWithLocationList = chBidirectionalDijkstra.getPaths(requests);
      }
      else {
        routeWithLocationList = chBidirectionalDijkstra.getPathsWithExecutor(requests, executors);
      }

      carsCreated = new ArrayList<>();
      for (int i = 0; i<requests.size(); i++) {
        Car car = carProvider.generateCar(mapFragment, routeWithLocationList.get(i));

        carProvider.limitSpeedPreventCollisionOnStart(car, startLanes.get(i));
        startLanes.get(i).addNewCar(car);
        carsCreated.add(car);
      }
    }

    log.info("Created cars {} with {}", carsCreated.size(), count);

  }

  @Override
  public void notify(Message message) {
    bigWorker = ((ServerInitializationMessage) message).isBigWorker();
  }
}
