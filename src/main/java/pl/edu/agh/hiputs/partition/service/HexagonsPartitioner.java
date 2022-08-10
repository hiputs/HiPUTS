package pl.edu.agh.hiputs.partition.service;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

    if (borderEdgesHandlingStrategy.equals(BorderEdgesHandlingStrategy.maxLaneLengthBuffer)) {
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
    Edge<JunctionData, WayData> e = edges.get(0);
    // wyznacz nowe koordynaty wierzchołka do cięcia
    HexagonCoordinate c1 = hexagonCoordinateFromPatchId(e.getSource().getData().getPatchId());
    HexagonCoordinate c2 = hexagonCoordinateFromPatchId(e.getTarget().getData().getPatchId());
    SlopeInterceptLine line = hexagonGrid.getLineBetween(c1, c2);
    Point.Double p1 = getPlanarPointFromNode(e.getSource());
    Point.Double p2 = getPlanarPointFromNode(e.getTarget());

    Point.Double intersectionPlainPoint = GeomUtil.calculateIntersectionPoint(p1, p2, line)
        .orElseThrow();

    double newLongitude = CoordinatesUtil.plain2Longitude(intersectionPlainPoint.getX(), intersectionPlainPoint.getY());
    double newLatitude = CoordinatesUtil.plain2Latitude(intersectionPlainPoint.getY());

    // utwórz nowy wirzchołek
    JunctionData junctionData = JunctionData.builder()
        .lon(newLongitude)
        .lat(newLatitude)
        .isCrossroad(false)
        .tags(new HashMap<>())
        .patchId(e.getData().getPatchId())
        .build();
    Node<JunctionData, WayData> newNode = new Node<>(UUID.randomUUID().toString(), junctionData);

    // utwórz lane-y
    Edge<JunctionData, WayData> edge1 = new Edge<>(UUID.randomUUID().toString(),
        WayData.builder()
            .length(CoordinatesUtil.plainDistanceInMeters(e.getSource().getData().getLat(), newLatitude, e.getSource().getData().getLon(), newLongitude))
            .patchId(e.getSource().getData().getPatchId())
            .isPriorityRoad(e.getData().isPriorityRoad())
            .isOneWay(e.getData().isOneWay())
            .maxSpeed(e.getData().getMaxSpeed())
            .tags(e.getData().getTags())
            .build());
    edge1.setSource(e.getSource());
    edge1.setTarget(newNode);

    Edge<JunctionData, WayData> edge2 = new Edge<>(UUID.randomUUID().toString(),
        WayData.builder()
            .length(CoordinatesUtil.plainDistanceInMeters(e.getTarget().getData().getLat(), newLatitude, e.getTarget().getData().getLon(), newLongitude))
            .patchId(e.getTarget().getData().getPatchId())
            .isPriorityRoad(e.getData().isPriorityRoad())
            .isOneWay(e.getData().isOneWay())
            .maxSpeed(e.getData().getMaxSpeed())
            .tags(e.getData().getTags())
            .build());
    edge2.setSource(newNode);
    edge2.setTarget(e.getTarget());

    Edge<JunctionData, WayData> edge3 = new Edge<>(UUID.randomUUID().toString(),
        WayData.builder()
            .length(CoordinatesUtil.plainDistanceInMeters(e.getSource().getData().getLat(), newLatitude, e.getSource().getData().getLon(), newLongitude))
            .patchId(e.getTarget().getData().getPatchId())
            .isPriorityRoad(e.getData().isPriorityRoad())
            .isOneWay(e.getData().isOneWay())
            .maxSpeed(e.getData().getMaxSpeed())
            .tags(e.getData().getTags())
            .build());
    edge3.setSource(e.getTarget());
    edge3.setTarget(newNode);

    Edge<JunctionData, WayData> edge4 = new Edge<>(UUID.randomUUID().toString(),
        WayData.builder()
            .length(CoordinatesUtil.plainDistanceInMeters(e.getTarget().getData().getLat(), newLatitude, e.getTarget().getData().getLon(), newLongitude))
            .patchId(e.getSource().getData().getPatchId())
            .isPriorityRoad(e.getData().isPriorityRoad())
            .isOneWay(e.getData().isOneWay())
            .maxSpeed(e.getData().getMaxSpeed())
            .tags(e.getData().getTags())
            .build());
    edge4.setSource(newNode);
    edge4.setTarget(e.getSource());

    // usuń stare lane'y
    edges.forEach(edge -> graph.removeEdgeById(edge.getId()));

    // dodaj nowy wierzchołek i edge do grafu
    graph.addNode(newNode);
    graph.addEdge(edge1);
    graph.addEdge(edge2);
    graph.addEdge(edge3);
    graph.addEdge(edge4);
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
