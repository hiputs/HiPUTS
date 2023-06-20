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
import pl.edu.agh.hiputs.service.pathfinder.CHBidirectionalAStar;
import pl.edu.agh.hiputs.service.pathfinder.CHBidirectionalDijkstra;
import pl.edu.agh.hiputs.service.pathfinder.PathFinder;
import pl.edu.agh.hiputs.service.pathfinder.RouteCarProvider;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarGeneratorService implements Subscriber {

  private static final int START_ADD_CAR = 5;
  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private final Configuration configuration = ConfigurationService.getConfiguration();
  private boolean bigWorker = false;
  private int step = 0;
  private int totalPatch = -1;


  private final Configuration.PathGeneratorConfiguration pathGeneratorConfiguration =
          configuration.getPathGeneratorConfiguration();

  private PathFinder<LaneId> pathFinder = null;
  ThreadPoolExecutor executors;

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
    if(totalPatch == -1) {
      totalPatch = mapRepository.getAllPatches().size();
    }

    if (pathFinder == null && !pathGeneratorConfiguration.getGeneratorType().equals("random")) {
      initPathFindingAlgorithm(mapFragment);
    }

    if(configuration.getNewCars() == 0) {
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
      count = (count * 10);
    }

    List<Car> carsCreated;
    if (pathFinder == null) {
      List<LaneEditable> lanesEditable = mapFragment.getRandomLanesEditable(count);
      ExampleCarProvider carProvider = new ExampleCarProvider(mapFragment, mapRepository);

      carsCreated = lanesEditable.parallelStream().map(lane -> {
        int hops = ThreadLocalRandom.current().nextInt(20, 40);

        if (bigWorker) {
          hops = 20;
        }
        Car car = carProvider.generateCar(lane.getLaneId(), hops);
        carProvider.limitSpeedPreventCollisionOnStart(car, lane);
        return placeCarOnLane(lane, car);
      }).filter(Objects::nonNull).toList();
    } else {
      RouteCarProvider carProvider = new RouteCarProvider();
      Set<LaneId> lanes = mapFragment.getLocalLaneIds();
      LaneId[] laneIds = lanes.toArray(new LaneId[0]);

      List<Pair<LaneId, LaneId>> requests = new ArrayList<>();
      for (int i=0; i<count; i++) {
        requests.add(new Pair<>(
                mapFragment.getLaneEditable(laneIds[ThreadLocalRandom.current().nextInt(0, laneIds.length)]).getLaneId(),
                mapFragment.getLaneEditable(laneIds[ThreadLocalRandom.current().nextInt(0, laneIds.length)]).getLaneId()
        ));
      }

      List<RouteWithLocation> routeWithLocationList;
      if (pathGeneratorConfiguration.getThreadsForExecutors() == 0) {
        routeWithLocationList = pathFinder.getPaths(requests);
      }
      else {
        routeWithLocationList = pathFinder.getPathsWithExecutor(requests, executors);
      }

      carsCreated = routeWithLocationList.stream().map(
              routeWithLocation -> {
                Car car = carProvider.generateCar(mapFragment, routeWithLocation);
                LaneEditable startLine = mapFragment.getLaneEditable(car.getLaneId());
                carProvider.limitSpeedPreventCollisionOnStart(car, startLine);
                return placeCarOnLane(startLine, car);
              }
      ).filter(Objects::nonNull).toList();
    }

    log.info("Created cars {} with {}", carsCreated.size(), count);
  }

  private void initPathFindingAlgorithm(MapFragment mapFragment) {
    if (pathGeneratorConfiguration.getGeneratorType().equals("cHBidirectionalDijkstra")) {
      if (totalPatch > 0 && mapRepository.isReady()) {
        pathFinder = new CHBidirectionalDijkstra(mapRepository, executors);
      } else {
        pathFinder = new CHBidirectionalDijkstra(mapFragment, executors);
      }
    } else if (pathGeneratorConfiguration.getGeneratorType().equals("cHBidirectionalAStar")) {
      if (totalPatch > 0 && mapRepository.isReady()) {
        pathFinder = new CHBidirectionalAStar(mapRepository, executors);
      } else {
        pathFinder = new CHBidirectionalAStar(mapFragment, executors);
      }
    }
  }

  private Car placeCarOnLane(LaneEditable lane, Car car) {
    if (car == null) {
      return null;
    }
    lane.addNewCar(car);
    return car;
  }

  @Override
  public void notify(Message message) {
    bigWorker = ((ServerInitializationMessage) message).isBigWorker();
  }
}
