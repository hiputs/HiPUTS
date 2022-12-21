package pl.edu.agh.hiputs.partition.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.geom.GeomUtil;
import pl.edu.agh.hiputs.partition.model.geom.HexagonCoordinate;
import pl.edu.agh.hiputs.partition.model.geom.HexagonGrid;
import pl.edu.agh.hiputs.partition.model.geom.HexagonGrid.HexagonLineSegment;
import pl.edu.agh.hiputs.partition.model.geom.LineSegment;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever.MapBoundaries;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.geom.StandardEquationLine;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Slf4j
@AllArgsConstructor
public class HexagonsPartitioner implements PatchPartitioner {

  @NonNull
  private final BorderEdgesHandlingStrategy borderEdgesHandlingStrategy;
  private double carViewRange;

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    colorGraph(graph);
    Graph<PatchData, PatchConnectionData> patchesGraph = new PatchesGraphExtractor().createFrom(graph);
    //log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  void colorGraph(Graph<JunctionData, WayData> graph) {
    MapBoundaries mapBoundaries = retrieveMapBoundaries(graph);

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.hybrid)) {
      LinkedList<Edge<JunctionData, WayData>> edgesToSplit = graph.getEdges().values().stream()
          .sorted(Comparator.comparing(e -> -e.getData().getLength()))
          .limit((int) (graph.getEdges().size() * 0.1))
          .collect(Collectors.toCollection(LinkedList::new));

      double lastLength = edgesToSplit.getLast().getData().getLength();

      edgesToSplit.stream()
          .filter(e -> !(e.getData().getLength() == lastLength))
          .collect(Collectors.groupingBy(e -> Stream.of(e.getSource().getId(), e.getTarget().getId()).sorted().collect(
              Collectors.joining("---"))))
          .forEach((k,v) -> {
            Edge<JunctionData, WayData> e = v.get(0);
            double newLongitude = (e.getSource().getData().getLon() + e.getTarget().getData().getLon()) / 2;
            double newLatitude = (e.getSource().getData().getLat() + e.getTarget().getData().getLat()) / 2;
            cutEdgesAtPoint(graph, v, newLongitude, newLatitude);
          });
    }

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.maxLaneLengthBuffer)
        ||  borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.hybrid)) {
      double maxLaneLength = graph.getEdges().values().stream()
          .map(e -> e.getData().getLength()).max(java.lang.Double::compareTo)
          .orElse(0.0);
      carViewRange += maxLaneLength;
    }

    HexagonGrid hexagonGrid = new HexagonGrid(mapBoundaries.getLeftBottomPlanarX(), mapBoundaries.getLeftBottomPlanarY(), carViewRange);
    List<Node<JunctionData, WayData>> graphSources = new LinkedList<>();
    graph.getNodes().values()
        .forEach(node -> {
          if (node.getIncomingEdges().size() == 0){
            graphSources.add(node);
            return;
          }
          double x = CoordinatesUtil.longitude2plain(node.getData().getLon(), node.getData().getLat());
          double y = CoordinatesUtil.latitude2plain(node.getData().getLat());
          HexagonCoordinate hexagonCoordinate = hexagonGrid.getHexagonCoordinate(x, y);
          String patchId = patchIdFromHexagonCoordinate(hexagonCoordinate);
          node.getData().setPatchId(patchId);
          node.getIncomingEdges().forEach(edge -> edge.getData().setPatchId(patchId));
        });

    //handle graphSources
    graphSources.forEach(node -> {
      node.getData().setPatchId(node.getOutgoingEdges().get(0).getData().getPatchId());
    });

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.edgeCutting)) {
      graph.getEdges().values().stream()
          .filter(e -> !e.getSource().getData().getPatchId().equals(e.getTarget().getData().getPatchId()))
          .collect(Collectors.groupingBy(e -> Stream.of(e.getSource().getId(), e.getTarget().getId()).sorted().collect(
              Collectors.joining("---"))))
          .forEach((k,v)-> cutEdges(graph, v, hexagonGrid));
    }
  }

  private MapBoundaries retrieveMapBoundaries(Graph<JunctionData, WayData> graph) {
    return MapBoundariesRetriever.retrieveMapBoundaries(graph);
  }

  private void cutEdges(Graph<JunctionData, WayData> graph, List<Edge<JunctionData, WayData>> edges, HexagonGrid hexagonGrid) {
    HexagonCoordinate c1 = hexagonCoordinateFromPatchId(edges.get(0).getSource().getData().getPatchId());
    HexagonCoordinate c2 = hexagonCoordinateFromPatchId(edges.get(0).getTarget().getData().getPatchId());

    //recursion end condition
    if (Objects.equals(c1, c2)) {
      return;
    }

    if (hexagonGrid.areNeighbours(c1, c2)) {
      cutEdgesOnce(graph, edges, hexagonGrid);
    } else {
      cutEdgesRecursive(graph, edges, hexagonGrid);
    }
  }

  private void cutEdgesOnce(Graph<JunctionData, WayData> graph, List<Edge<JunctionData, WayData>> edges, HexagonGrid hexagonGrid) {
    Point intersectionPlainPoint = calculateNewNodePoint(edges.get(0), hexagonGrid);

    double newLongitude = CoordinatesUtil.plain2Longitude(intersectionPlainPoint.getX(), intersectionPlainPoint.getY());
    double newLatitude = CoordinatesUtil.plain2Latitude(intersectionPlainPoint.getY());

    cutEdgesAtPoint(graph, edges, newLongitude, newLatitude);
  }

  private void cutEdgesRecursive(Graph<JunctionData, WayData> graph, List<Edge<JunctionData, WayData>> edges, HexagonGrid hexagonGrid) {
    PointWithNextHex intersectionPlainPoint = calculateNewNodePointForRecursive(edges.get(0), hexagonGrid);

    double newLongitude = CoordinatesUtil.plain2Longitude(intersectionPlainPoint.getPoint().getX(), intersectionPlainPoint.getPoint().getY());
    double newLatitude = CoordinatesUtil.plain2Latitude(intersectionPlainPoint.getPoint().getY());

    edges = cutEdgesAtPointForRecursive(graph, edges, newLongitude, newLatitude, patchIdFromHexagonCoordinate(intersectionPlainPoint.nextHexagonCoordinate));
    cutEdges(graph, edges, hexagonGrid);
  }

  private void cutEdgesAtPoint(
      Graph<JunctionData, WayData> graph,
      List<Edge<JunctionData, WayData>> edges,
      double newLongitude,
      double newLatitude) {
    Edge<JunctionData, WayData> e = edges.get(0);
    Node<JunctionData, WayData> newNode = createChildNode(newLongitude, newLatitude, e.getData().getPatchId());
    graph.addNode(newNode);

    edges.forEach(edge -> graph.removeEdgeById(edge.getId()));

    edges.forEach(edge -> {
      Edge<JunctionData, WayData> edge1 = createChildEdge(edge.getSource(), newNode, edge, edge.getSource().getData().getPatchId());
      Edge<JunctionData, WayData> edge2 = createChildEdge(newNode, edge.getTarget(), edge, edge.getTarget().getData().getPatchId());

      graph.addEdge(edge1);
      graph.addEdge(edge2);
    });
  }

  private List<Edge<JunctionData, WayData>> cutEdgesAtPointForRecursive(
      Graph<JunctionData, WayData> graph,
      List<Edge<JunctionData, WayData>> edges,
      double newLongitude,
      double newLatitude,
      String patchId) {
    Edge<JunctionData, WayData> e = edges.get(0);
    Node<JunctionData, WayData> newNode = createChildNode(newLongitude, newLatitude, patchId);
    graph.addNode(newNode);

    edges.forEach(edge -> graph.removeEdgeById(edge.getId()));

    List<Edge<JunctionData, WayData>> remainingEdges = new LinkedList<>();

    edges.forEach(edge -> {
      Edge<JunctionData, WayData> edge1;
      Edge<JunctionData, WayData> edge2;

      if (edge.getSource().equals(e.getSource())) {
        edge1 = createChildEdge(edge.getSource(), newNode, edge, edge.getSource().getData().getPatchId());
        edge2 = createChildEdge(newNode, edge.getTarget(), edge, patchId);
        remainingEdges.add(edge2);
      } else {
        edge1 = createChildEdge(edge.getSource(), newNode, edge, patchId);
        edge2 = createChildEdge(newNode, edge.getTarget(), edge, edge.getTarget().getData().getPatchId());
        remainingEdges.add(edge1);
      }

      graph.addEdge(edge1);
      graph.addEdge(edge2);
    });

    return remainingEdges;
  }

  private Point calculateNewNodePoint(Edge<JunctionData, WayData> e, HexagonGrid hexagonGrid) {
    HexagonCoordinate c1 = hexagonCoordinateFromPatchId(e.getSource().getData().getPatchId());
    HexagonCoordinate c2 = hexagonCoordinateFromPatchId(e.getTarget().getData().getPatchId());
    Point p1 = getPlanarPointFromNode(e.getSource());
    Point p2 = getPlanarPointFromNode(e.getTarget());
    StandardEquationLine line = hexagonGrid.getLineBetween(c1, c2);

    return GeomUtil.calculateIntersectionPoint(p1, p2, line).orElseThrow();
  }

  private PointWithNextHex calculateNewNodePointForRecursive(Edge<JunctionData, WayData> e, HexagonGrid hexagonGrid) {
    HexagonCoordinate c1 = hexagonCoordinateFromPatchId(e.getSource().getData().getPatchId());
    Point p1 = getPlanarPointFromNode(e.getSource());
    Point p2 = getPlanarPointFromNode(e.getTarget());

    List<HexagonLineSegment> hexagonLineSegments = hexagonGrid.getLineSegmentsOfHexagon(c1);
    LineSegment edgeLineSegment = new LineSegment(p1, p2);
    Map<HexagonLineSegment, Optional<Point>> intersectionPointsWithEdgeLineSegments = hexagonLineSegments.stream()
        .collect(Collectors.toMap(ls -> ls, ls -> ls.intersectionPointWith(edgeLineSegment)))
        .entrySet().stream()
        .filter(entry -> entry.getValue().isPresent())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    if (intersectionPointsWithEdgeLineSegments.size() != 1) {
      //Cannot clearly find cut point - selecting the most distant one from source
      intersectionPointsWithEdgeLineSegments = intersectionPointsWithEdgeLineSegments.entrySet().stream()
          .sorted(Comparator.comparing(entry ->  - entry.getValue().get().distanceTo(p1)))
          .limit(1)
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    Entry<HexagonLineSegment, Optional<Point>> resEntry = intersectionPointsWithEdgeLineSegments.entrySet()
        .stream()
        .toList()
        .get(0);
    return new PointWithNextHex(resEntry.getValue().get(), hexagonGrid.getNeighbourHexagonCoordinate(c1, resEntry.getKey().getOrientation()));
  }

  private Node<JunctionData, WayData> createChildNode(double lon, double lat, String patchId) {
    return new Node<>(UUID.randomUUID().toString(),
        JunctionData.builder()
            .lon(lon)
            .lat(lat)
            .isCrossroad(false)
            .tags(new HashMap<>())
            .patchId(patchId)
            .isOsmNode(false)
            .build());
  }

  private Edge<JunctionData, WayData> createChildEdge(
      Node<JunctionData, WayData> source,
      Node<JunctionData, WayData> target,
      Edge<JunctionData, WayData> parentEdge,
      String patchId) {
    Edge<JunctionData, WayData> newEdge = new Edge<>(source.getId() + "->" + target.getId(),
        WayData.builder()
            .length(CoordinatesUtil.plainDistanceInMeters(source.getData().getLat(), target.getData().getLat(), source.getData().getLon(), target.getData().getLon()))
            .patchId(patchId)
            .isPriorityRoad(parentEdge.getData().isPriorityRoad())
            .isOneWay(parentEdge.getData().isOneWay())
            .maxSpeed(parentEdge.getData().getMaxSpeed())
            .tags(parentEdge.getData().getTags())
            .build());
    newEdge.setSource(source);
    newEdge.setTarget(target);
    return newEdge;
  }


  private static String patchIdFromHexagonCoordinate(HexagonCoordinate hexagonCoordinate) {
    return String.format("%d-%d", hexagonCoordinate.getXHex(), hexagonCoordinate.getYHex());
  }

  private static HexagonCoordinate hexagonCoordinateFromPatchId(String patchId) {
    String[] arr = patchId.split("-");
    return HexagonCoordinate.builder()
            .xHex(Integer.parseInt(arr[0]))
            .yHex(Integer.parseInt(arr[1]))
            .build();
  }

  private Point getPlanarPointFromNode(Node<JunctionData, WayData> node) {
    double x = CoordinatesUtil.longitude2plain(node.getData().getLon(), node.getData().getLat());
    double y = CoordinatesUtil.latitude2plain(node.getData().getLat());
    return new Point(x, y);
  }

  public enum BorderEdgesHandlingStrategy {
    edgeCutting,
    maxLaneLengthBuffer,
    hybrid
  }

  @Getter
  @Setter
  @AllArgsConstructor
  private static class PointWithNextHex {
    private Point point;
    private HexagonCoordinate nextHexagonCoordinate;
  }
}
