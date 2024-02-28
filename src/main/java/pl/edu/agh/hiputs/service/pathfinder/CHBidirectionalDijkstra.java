package pl.edu.agh.hiputs.service.pathfinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
public class CHBidirectionalDijkstra implements PathFinder<RoadId> {

  private final Map<RoadId, Pair<JunctionReadable, JunctionReadable>> roadIdToJunctionsMapping = new HashMap<>();
  private final ArrayList<RoadId> roadIds = new ArrayList<>();
  private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, RoadReadable> ch;
  private ContractionHierarchyBidirectionalDijkstra<JunctionReadable, RoadReadable> dijkstraShortestPath;

  private Optional<MapRepository> mapRepository = Optional.empty();

  private Optional<MapFragment> mapFragment = Optional.empty();

  public CHBidirectionalDijkstra(MapRepository mapRepository, ThreadPoolExecutor executor) {
    this.mapRepository = Optional.of(mapRepository);
    Graph<JunctionReadable, RoadReadable> graph = createGraphFromMapRepository(mapRepository);
    ch = createCHBidirectionalDijkstra(graph, executor);
    dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    roadIds.addAll(roadIdToJunctionsMapping.keySet());
  }

  private Graph<JunctionReadable, RoadReadable> createGraphFromMapRepository(MapRepository mapRepository) {
    Graph<JunctionReadable, RoadReadable> graph = new SimpleDirectedWeightedGraph<>(RoadReadable.class);
    List<Patch> patches = mapRepository.getAllPatches();
    for (Patch patch : patches) {
      addPatchDataToGraph(patch, graph);
    }
    return graph;
  }

  private void addPatchDataToGraph(PatchReader patch, Graph<JunctionReadable, RoadReadable> graph) {
    patch.getLaneIds();
    for (RoadId roadId : patch.getRoadIds()) {
      RoadReadable roadReadable = patch.getRoadReadable(roadId);

      JunctionReadable incomingJunction = getJunctionReadable(roadReadable.getIncomingJunctionId(), patch);
      JunctionReadable outgoingJunction = getJunctionReadable(roadReadable.getOutgoingJunctionId(), patch);
      roadIdToJunctionsMapping.put(roadId, new Pair<>(incomingJunction, outgoingJunction));
      if (!graph.containsVertex(incomingJunction)) {
        graph.addVertex(incomingJunction);
      }
      if (!graph.containsVertex(outgoingJunction)) {
        graph.addVertex(outgoingJunction);
      }
      if (!graph.containsEdge(roadReadable)) {
        graph.addEdge(incomingJunction, outgoingJunction, roadReadable);
        graph.setEdgeWeight(roadReadable, roadReadable.getLength());
      }
    }
  }

  private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, RoadReadable> createCHBidirectionalDijkstra(
      Graph<JunctionReadable, RoadReadable> graph, ThreadPoolExecutor executor) {
    ContractionHierarchyPrecomputation<JunctionReadable, RoadReadable> precomputation =
        new ContractionHierarchyPrecomputation<>(graph, executor);

    return precomputation.computeContractionHierarchy();
  }

  public CHBidirectionalDijkstra(MapFragment mapFragment, ThreadPoolExecutor executor) {
    this.mapFragment = Optional.of(mapFragment);
    Graph<JunctionReadable, RoadReadable> graph = createGraphFromMapFragment(mapFragment);
    ch = createCHBidirectionalDijkstra(graph, executor);
    dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    roadIds.addAll(roadIdToJunctionsMapping.keySet());
  }

  private Graph<JunctionReadable, RoadReadable> createGraphFromMapFragment(MapFragment mapFragment) {
    Graph<JunctionReadable, RoadReadable> graph = new SimpleDirectedWeightedGraph<>(RoadReadable.class);
    Set<PatchReader> patches = mapFragment.getKnownPatchReadable();
    for (PatchReader patch : patches) {
      addPatchDataToGraph(patch, graph);
    }
    return graph;
  }

  public CHBidirectionalDijkstra(String precomputationFilePath) {
    try {
      ch = readObjectFromFile(precomputationFilePath);
      dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    } catch (Exception e) {
      log.error("Cannot read CHBidirectionalDijkstra precomputation:" + e);
    }
    roadIds.addAll(roadIdToJunctionsMapping.keySet());
  }

  // Deserialization
  // Get object from a file.
  // TODO implement deserialization
  public static ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, RoadReadable> readObjectFromFile(
      String filePath) throws IOException, ClassNotFoundException {
    return null;
  }

  private JunctionReadable getJunctionReadable(JunctionId id, PatchReader patch) {
    if (mapRepository.isPresent()) {
      return mapRepository.get().getJunctionReadable(id);
    } else if (mapFragment.isPresent()) {
      return mapFragment.get().getJunctionReadable(id);
    } else {
      return patch.getJunctionReadable(id);
    }
  }

  // Serialization
  // Save object into a file.
  // TODO implement serialization
  public void dumpPrecomputationToFile(String filePath) throws IOException {
  }

  @Override
  public List<RouteWithLocation> getPaths(List<Pair<RoadId, RoadId>> requests) {
    log.debug("In getPath");
    List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
    for (Pair<RoadId, RoadId> request : requests) {
      routeWithLocationList.add(getPath(request));
    }
    return routeWithLocationList;
  }

  @Override
  public RouteWithLocation getPath(Pair<RoadId, RoadId> request) {
    // System.out.println("Current thread: " + Thread.currentThread());
    JunctionReadable incomingJunction = roadIdToJunctionsMapping.get(request.getFirst()).getFirst();
    JunctionReadable outgoingJunction = roadIdToJunctionsMapping.get(request.getSecond()).getSecond();
    JunctionReadable endingJunction;
    if (incomingJunction.getJunctionId().equals(outgoingJunction.getJunctionId())) {
      endingJunction = roadIdToJunctionsMapping.get(request.getSecond()).getFirst();
    } else {
      endingJunction = incomingJunction;
    }
    GraphPath<JunctionReadable, RoadReadable> graphPath = findRoute(incomingJunction, endingJunction);

    LinkedList<RouteElement> routeElements = new LinkedList<>();
    if (graphPath == null) {
      return new RouteWithLocation(routeElements, 0);
    }
    int size = graphPath.getEdgeList().size();
    for (int i = 0; i < size; i++) {
      routeElements.addLast(new RouteElement(graphPath.getVertexList().get(i).getJunctionId(),
          graphPath.getEdgeList().get(i).getRoadId()));
    }
    if (!outgoingJunction.equals(endingJunction)) {
      routeElements.addLast(new RouteElement(outgoingJunction.getJunctionId(), request.getSecond()));
    }

    return new RouteWithLocation(routeElements, 0);
  }

  private GraphPath<JunctionReadable, RoadReadable> findRoute(JunctionReadable source, JunctionReadable sink) {
    return dijkstraShortestPath.getPath(source, sink);
  }

  @Override
  public List<RouteWithLocation> getPathsWithExecutor(List<Pair<RoadId, RoadId>> requests,
      ThreadPoolExecutor executor) {
    log.debug("In getPath executors");
    List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    for (Pair<RoadId, RoadId> request : requests) {
      GetPathCallable callable = new GetPathCallable(request, this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i = 0; i < requests.size(); i++) {
        routeWithLocationList.add(futureRoutesWithLocation.get(i).get());
      }
    } catch (Exception e) {
      log.error("Cannot collect future", e);
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<RoadId, RouteWithLocation>> getPathsToRandomSink(List<RoadId> starts) {
    List<Pair<RoadId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    for (RoadId start : starts) {
      RoadId end = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      routeWithLocationList.add(new Pair<>(end, getPath(new Pair<>(start, end))));
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<RoadId, RouteWithLocation>> getPathsToRandomSinkWithExecutor(List<RoadId> starts,
      ThreadPoolExecutor executor) {
    List<Pair<RoadId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    List<RoadId> ends = new ArrayList<>();
    for (RoadId start : starts) {
      RoadId end = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      ends.add(end);
      GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i = 0; i < starts.size(); i++) {
        routeWithLocationList.add(new Pair<>(ends.get(i), futureRoutesWithLocation.get(i).get()));
      }
    } catch (Exception e) {
      System.out.println("Cannot collect future\n" + e);
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<Pair<RoadId, RoadId>, RouteWithLocation>> getRandomPaths(int n) {
    List<Pair<Pair<RoadId, RoadId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      RoadId start = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      RoadId end = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      routeWithLocationList.add(new Pair<>(new Pair<>(start, end), getPath(new Pair<>(start, end))));
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<Pair<RoadId, RoadId>, RouteWithLocation>> getRandomPathsWithExecutor(int n,
      ThreadPoolExecutor executor) {
    List<Pair<Pair<RoadId, RoadId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    List<Pair<RoadId, RoadId>> requests = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      RoadId start = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      RoadId end = roadIds.get(ThreadLocalRandom.current().nextInt(roadIds.size()));
      requests.add(new Pair<>(start, end));
      GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i = 0; i < n; i++) {
        routeWithLocationList.add(new Pair<>(requests.get(i), futureRoutesWithLocation.get(i).get()));
      }
    } catch (Exception e) {
      System.out.println("Cannot collect future\n" + e);
    }
    return routeWithLocationList;
  }

  public static class GetPathCallable implements Callable<RouteWithLocation> {

    Pair<RoadId, RoadId> request;
    CHBidirectionalDijkstra chBidirectionalDijkstra;

    public GetPathCallable(Pair<RoadId, RoadId> request, CHBidirectionalDijkstra chBidirectionalDijkstra) {
      this.request = request;
      this.chBidirectionalDijkstra = chBidirectionalDijkstra;
    }

    public RouteWithLocation call() {
      return chBidirectionalDijkstra.getPath(request);
    }
  }
}