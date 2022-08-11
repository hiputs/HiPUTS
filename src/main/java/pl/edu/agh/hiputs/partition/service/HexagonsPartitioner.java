package pl.edu.agh.hiputs.partition.service;

import java.awt.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.service.util.GeomUtil;
import pl.edu.agh.hiputs.partition.service.util.HexagonCoordinate;
import pl.edu.agh.hiputs.partition.service.util.HexagonGrid;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever;
import pl.edu.agh.hiputs.partition.service.util.MapBoundariesRetriever.MapBoundaries;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;
import pl.edu.agh.hiputs.partition.service.util.SlopeInterceptLine;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Slf4j
@AllArgsConstructor
public class HexagonsPartitioner implements PatchPartitioner {

  @NonNull
  private final BorderEdgesHandlingStrategy borderEdgesHandlingStrategy;
  private double carViewRange;

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    MapBoundaries mapBoundaries = retrieveMapBoundaries(graph);

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.hybrid)) {
      List<Edge<JunctionData, WayData>> edgesToSplit = graph.getEdges().values().stream()
          .sorted(Comparator.comparing(e -> -e.getData().getLength()))
          .limit((int) (graph.getEdges().size() * 0.1))
          .toList();

      edgesToSplit.stream().collect(Collectors.groupingBy(e -> Stream.of(e.getSource().getId(), e.getTarget().getId()).sorted().collect(
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
    graph.getNodes().values()
        .forEach(node -> {
          double x = CoordinatesUtil.longitude2plain(node.getData().getLon(), node.getData().getLat());
          double y = CoordinatesUtil.latitude2plain(node.getData().getLat());
          HexagonCoordinate hexagonCoordinate = hexagonGrid.getHexagonCoordinate(x, y);
          String patchId = String.format("%d-%d", hexagonCoordinate.getXHex(), hexagonCoordinate.getYHex());
          node.getData().setPatchId(patchId);
          node.getIncomingEdges().forEach(edge -> edge.getData().setPatchId(patchId));
        });

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.edgeCutting)) {
      graph.getEdges().values().stream()
          .filter(e -> !e.getSource().getData().getPatchId().equals(e.getTarget().getData().getPatchId()))
          .collect(Collectors.groupingBy(e -> Stream.of(e.getSource().getId(), e.getTarget().getId()).sorted().collect(
              Collectors.joining("---"))))
          .forEach((k,v)-> cutEdges(graph, v, hexagonGrid));
    }

    Graph<PatchData, PatchConnectionData> patchesGraph = new PatchesGraphExtractor().createFrom(graph);
    log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  private MapBoundaries retrieveMapBoundaries(Graph<JunctionData, WayData> graph) {
    return MapBoundariesRetriever.retrieveMapBoundaries(graph);
  }

  private void cutEdges(Graph<JunctionData, WayData> graph, List<Edge<JunctionData, WayData>> edges, HexagonGrid hexagonGrid) {
    Point.Double intersectionPlainPoint = calculateNewNodePoint(edges.get(0), hexagonGrid);

    double newLongitude = CoordinatesUtil.plain2Longitude(intersectionPlainPoint.getX(), intersectionPlainPoint.getY());
    double newLatitude = CoordinatesUtil.plain2Latitude(intersectionPlainPoint.getY());

    cutEdgesAtPoint(graph, edges, newLongitude, newLatitude);
  }

  private void cutEdgesAtPoint(Graph<JunctionData, WayData> graph, List<Edge<JunctionData, WayData>> edges, double newLongitude, double newLatitude) {
    Edge<JunctionData, WayData> e = edges.get(0);
    Node<JunctionData, WayData> newNode = createChildNode(newLongitude, newLatitude, e.getData().getPatchId());
    graph.addNode(newNode);

    edges.forEach(edge -> graph.removeEdgeById(edge.getId()));

    edges.forEach(edge -> {
      Edge<JunctionData, WayData> edge1 = createChildEdge(e.getSource(), newNode, e, e.getSource().getData().getPatchId());
      Edge<JunctionData, WayData> edge2 = createChildEdge(newNode, e.getTarget(), e, e.getTarget().getData().getPatchId());

      graph.addEdge(edge1);
      graph.addEdge(edge2);
    });
  }

  private Point.Double calculateNewNodePoint(Edge<JunctionData, WayData> e, HexagonGrid hexagonGrid) {
    HexagonCoordinate c1 = hexagonCoordinateFromPatchId(e.getSource().getData().getPatchId());
    HexagonCoordinate c2 = hexagonCoordinateFromPatchId(e.getTarget().getData().getPatchId());
    SlopeInterceptLine line = hexagonGrid.getLineBetween(c1, c2);
    Point.Double p1 = getPlanarPointFromNode(e.getSource());
    Point.Double p2 = getPlanarPointFromNode(e.getTarget());

    return GeomUtil.calculateIntersectionPoint(p1, p2, line)
        .orElseThrow();
  }

  private Node<JunctionData, WayData> createChildNode(double lon, double lat, String patchId) {
    return new Node<>(UUID.randomUUID().toString(),
        JunctionData.builder()
            .lon(lon)
            .lat(lat)
            .isCrossroad(false)
            .tags(new HashMap<>())
            .patchId(patchId)
            .build());
  }

  private Edge<JunctionData, WayData> createChildEdge(
      Node<JunctionData, WayData> source,
      Node<JunctionData, WayData> target,
      Edge<JunctionData, WayData> parentEdge,
      String patchId) {
    Edge<JunctionData, WayData> newEdge = new Edge<>(UUID.randomUUID().toString(),
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

  private HexagonCoordinate hexagonCoordinateFromPatchId(String patchId) {
    String[] arr = patchId.split("-");
    return HexagonCoordinate.builder()
            .xHex(Integer.parseInt(arr[0]))
            .yHex(Integer.parseInt(arr[1]))
            .build();
  }

  private Point.Double getPlanarPointFromNode(Node<JunctionData, WayData> node) {
    double x = CoordinatesUtil.longitude2plain(node.getData().getLon(), node.getData().getLat());
    double y = CoordinatesUtil.latitude2plain(node.getData().getLat());
    return new Point.Double(x, y);
  }

  public enum BorderEdgesHandlingStrategy {
    edgeCutting,
    maxLaneLengthBuffer,
    hybrid
  }
}
