package pl.edu.agh.hiputs.example;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
public class SquareMapUniformCarProvider extends ExampleCarProvider{

  private final int junctionsLat;
  private final int junctionsLong;
  private final double laneLength;
  private final List<List<JunctionReadable>> junctions;
  private final LinkedList<Pair<LaneId, Double>> carPositions;


  public SquareMapUniformCarProvider(MapFragment mapFragment, MapRepository mapRepository) {
    super(mapFragment, mapRepository);

     this.junctions = mapFragment.getLocalJunctionIds().stream()
         .map(mapFragment::getJunctionReadable)
        .collect(Collectors.groupingBy(JunctionReadable::getLatitude, Collectors.toList()))
        .values()
        .stream()
        .sorted(Comparator.comparing(list -> list.get(0).getLatitude()))
        .toList();

    this.junctions.forEach((v) -> v.sort(Comparator.comparing(JunctionReadable::getLongitude)));

    this.junctionsLat = this.junctions.size();
    this.junctionsLong = this.junctions.get(0).size();

    this.laneLength = mapRepository.getAllPatches().get(0).getAnyLane().getLength();
    this.carPositions = calculateUniformCarPositions();
  }

  private Pair<LaneId, Double> coordsToCarPosition(Double longitude, Double latitude){
    int latId = (int) (latitude / 500);    // map key
    int longId = (int) (longitude / 500);    // array id

    JunctionReadable junction1 = junctions.get(latId).get(longId);
    JunctionReadable junction2 = latitude > latId*laneLength ?
        junctions.get(Math.floorMod(latId+1,junctionsLat)).get(longId) :
        junctions.get(latId).get(Math.floorMod(longId+1,junctionsLong));

    LaneId lane = getCommonLanes(junction1, junction2)
        .get((int) (Math.random() * 2))
        .getLaneId();

    double position = latitude > latId*laneLength ?
        latitude - junction1.getLatitude()*100 :
        longitude - junction1.getLongitude()*100;

    return new ImmutablePair<>(lane,position);
  }

  private List<LaneOnJunction> getCommonLanes(JunctionReadable junction1, JunctionReadable junction2){
    return junction1.streamLanesOnJunction()
        .filter(lane1 -> junction2
            .streamLanesOnJunction()
            .anyMatch(lane2 -> lane2.getLaneId().getValue().equals(lane1.getLaneId().getValue())))
        .toList();
  }

  private LinkedList<Pair<LaneId, Double>> calculateUniformCarPositions() {
    float proportion = junctionsLong/(float)junctionsLat;
    int linesLat = (int) Math.ceil( Math.sqrt(configuration.getWorkerInitialNumberOfCars() / proportion));
    int linesLong = (int) Math.ceil( ((float) configuration.getWorkerInitialNumberOfCars()) / linesLat);

    double distanceLat = junctionsLat * laneLength / linesLat;
    double distanceLong = junctionsLong * laneLength / linesLong;

    LinkedList<Pair<LaneId, Double>> possibleCarCoords = new LinkedList<>();

    for(double i=laneLength/2; i<junctionsLong * laneLength; i+=distanceLong){
      for(double j=laneLength/4; j<junctionsLat * laneLength; j+=distanceLat){
        possibleCarCoords.add(coordsToCarPosition(i,j));
      }
    }
    return possibleCarCoords;
  }

  private Integer getDefaultHops() {
    int hops = (int) (configuration.getDefaultMaxSpeed()*configuration.getSimulationTimeStep()*configuration.getSimulationStep() / laneLength);
    return hops + 3 + (int) (0.1*hops);
  }

  public List<Car> generateManyCars() {
    return carPositions
        .subList(0,configuration.getWorkerInitialNumberOfCars())
        .stream()
        .map(position -> generateCar(position.getRight(), position.getLeft(), getDefaultHops()))
        .toList();
  }

}
