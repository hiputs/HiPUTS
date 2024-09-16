package pl.edu.agh.hiputs.service.pathfinder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.*;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class CHBidirectionalDijkstra implements PathFinder<LaneId> {
  private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch;
  private ContractionHierarchyBidirectionalDijkstra<JunctionReadable, LaneReadable> dijkstraShortestPath;
  private final Map<LaneId, Pair<JunctionReadable, JunctionReadable>> laneToJunctionsMapping = new HashMap<>();
  private final ArrayList<LaneId> laneIds = new ArrayList<>();

  public CHBidirectionalDijkstra(MapRepository mapRepository, ThreadPoolExecutor executor) {
    Graph<JunctionReadable, LaneReadable> graph = createGraphFromMapRepository(mapRepository);
    ch = createCHBidirectionalDijkstra(graph, executor);
    dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    laneIds.addAll(laneToJunctionsMapping.keySet());
  }

  public CHBidirectionalDijkstra(MapFragment mapFragment, ThreadPoolExecutor executor) {
    Graph<JunctionReadable, LaneReadable> graph = createGraphFromMapFragment(mapFragment);
    ch = createCHBidirectionalDijkstra(graph, executor);
    dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    laneIds.addAll(laneToJunctionsMapping.keySet());
  }

  public CHBidirectionalDijkstra(String precomputationFilePath) {
    try {
      ch = readObjectFromFile(precomputationFilePath);
      dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }
    catch(Exception e) {
      log.error("Cannot read CHBidirectionalDijkstra precomputation:" + e);
    }
    laneIds.addAll(laneToJunctionsMapping.keySet());
  }

  private Graph<JunctionReadable, LaneReadable> createGraphFromMapRepository(MapRepository mapRepository) {
    Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
    List<Patch> patches = mapRepository.getAllPatches();
    for (Patch patch: patches) {
      addPatchDataToGraph(patch, graph);
    }
    return graph;
  }

  private Graph<JunctionReadable, LaneReadable> createGraphFromMapFragment(MapFragment mapFragment) {
    Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
    Set<PatchReader> patches = mapFragment.getKnownPatchReadable();
    for (PatchReader patch: patches) {
      addPatchDataToGraph(patch, graph);
    }
    return graph;
  }

  private void addPatchDataToGraph(PatchReader patch, Graph<JunctionReadable, LaneReadable> graph) {
    patch.getLaneIds();
    for (LaneId laneId: patch.getLaneIds()) {
      LaneReadable laneReadable = patch.getLaneReadable(laneId);

      JunctionReadable incomingJunction = patch.getJunctionReadable(laneReadable.getIncomingJunctionId());
      JunctionReadable outgoingJunction = patch.getJunctionReadable(laneReadable.getOutgoingJunctionId());
      laneToJunctionsMapping.put(laneId, new Pair<>(incomingJunction, outgoingJunction));
      if (!graph.containsVertex(incomingJunction)) {
        graph.addVertex(incomingJunction);
      }
      if (!graph.containsVertex(outgoingJunction)) {
        graph.addVertex(outgoingJunction);
      }
      if (!graph.containsEdge(laneReadable)) {
        graph.addEdge(incomingJunction, outgoingJunction, laneReadable);
        graph.setEdgeWeight(laneReadable, laneReadable.getLength());
      }
    }
  }

  private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> createCHBidirectionalDijkstra(
    Graph<JunctionReadable, LaneReadable> graph,
    ThreadPoolExecutor executor) {
    ContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
      new ContractionHierarchyPrecomputation<>(graph, executor);

    return precomputation.computeContractionHierarchy();
  }

  // Serialization
  // Save object into a file.
  // TODO implement serialization
  public void dumpPrecomputationToFile(String filePath) throws IOException {
  }

  // Deserialization
  // Get object from a file.
  // TODO implement deserialization
  public static ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable>  readObjectFromFile(String filePath) throws IOException, ClassNotFoundException {
    return null;
  }

  private GraphPath<JunctionReadable, LaneReadable> findRoute(JunctionReadable source, JunctionReadable sink) {
    return dijkstraShortestPath.getPath(source, sink);
  }

  @Override
  public RouteWithLocation getPath(Pair<LaneId, LaneId> request) {
    // System.out.println("Current thread: " + Thread.currentThread());
    JunctionReadable incomingJunction = laneToJunctionsMapping.get(request.getFirst()).getFirst();
    JunctionReadable outgoingJunction = laneToJunctionsMapping.get(request.getSecond()).getSecond();
    JunctionReadable endingJunction;
    if (incomingJunction.getJunctionId().equals(outgoingJunction.getJunctionId())) {
      endingJunction = laneToJunctionsMapping.get(request.getSecond()).getFirst();
    }
    else {
      endingJunction = incomingJunction;
    }
    GraphPath<JunctionReadable, LaneReadable> graphPath = findRoute(incomingJunction, endingJunction);

    LinkedList<RouteElement> routeElements = new LinkedList<>();
    if (graphPath == null) {
      return new RouteWithLocation(routeElements, 0);
    }
    int size = graphPath.getEdgeList().size();
    for (int i=0; i<size; i++) {
      routeElements.addLast(new RouteElement(
        graphPath.getVertexList().get(i).getJunctionId(),
        graphPath.getEdgeList().get(i).getLaneId()));
    }
    if (!outgoingJunction.equals(endingJunction)) {
      routeElements.addLast(
        new RouteElement(
          outgoingJunction.getJunctionId(),
          request.getSecond()
        )
      );
    }

    return new RouteWithLocation(routeElements, 0);
  }

  @Override
  public List<RouteWithLocation> getPaths(List<Pair<LaneId, LaneId>> requests) {
    System.out.println("In getPath");
    List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
    for (Pair<LaneId, LaneId> request: requests) {
      routeWithLocationList.add(getPath(request));
    }
    return routeWithLocationList;
  }

  @Override
  public List<RouteWithLocation> getPathsWithExecutor(List<Pair<LaneId, LaneId>> requests, ThreadPoolExecutor executor) {
    System.out.println("In getPath executors");
    List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    for (Pair<LaneId, LaneId> request: requests) {
      GetPathCallable callable = new GetPathCallable(request, this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i = 0; i < requests.size(); i++) {
        routeWithLocationList.add(futureRoutesWithLocation.get(i).get());
      }
    }
    catch (Exception e) {
      System.out.println("Cannot collect future\n" + e);
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSink(List<LaneId> starts) {
    List<Pair<LaneId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    for (LaneId start: starts) {
      LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      routeWithLocationList.add(new Pair<>(end, getPath(new Pair<>(start, end))));
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSinkWithExecutor(List<LaneId> starts, ThreadPoolExecutor executor) {
    List<Pair<LaneId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    List<LaneId> ends = new ArrayList<>();
    for (LaneId start: starts) {
      LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      ends.add(end);
      GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i = 0; i < starts.size(); i++) {
        routeWithLocationList.add(new Pair<>(ends.get(i), futureRoutesWithLocation.get(i).get()));
      }
    }
    catch (Exception e) {
      System.out.println("Cannot collect future\n" + e);
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPaths(int n) {
    List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    for (int i=0; i<n; i++) {
      LaneId start = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      routeWithLocationList.add(new Pair<>(new Pair<>(start, end), getPath(new Pair<>(start, end))));
    }
    return routeWithLocationList;
  }

  @Override
  public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPathsWithExecutor(int n, ThreadPoolExecutor executor) {
    List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
    List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
    List<Pair<LaneId, LaneId>> requests = new ArrayList<>();
    for (int i=0; i<n; i++) {
      LaneId start = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
      requests.add(new Pair<>(start, end));
      GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
      futureRoutesWithLocation.add(executor.submit(callable));
    }
    try {
      for (int i=0; i<n; i++) {
        routeWithLocationList.add(new Pair<>(requests.get(i), futureRoutesWithLocation.get(i).get()));
      }
    }
    catch (Exception e) {
      System.out.println("Cannot collect future\n" + e);
    }
    return routeWithLocationList;
  }


  public static class GetPathCallable implements Callable<RouteWithLocation> {
    Pair<LaneId, LaneId> request;
    CHBidirectionalDijkstra chBidirectionalDijkstra;

    public GetPathCallable(Pair<LaneId, LaneId> request, CHBidirectionalDijkstra chBidirectionalDijkstra) {
      this.request = request;
      this.chBidirectionalDijkstra = chBidirectionalDijkstra;
    }

    public RouteWithLocation call() {
      return chBidirectionalDijkstra.getPath(request);
    }
  }
}