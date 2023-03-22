package pl.edu.agh.hiputs.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.lane.LanesProcessor;
import pl.edu.agh.hiputs.partition.mapper.util.lane.RelativeDirection;
import pl.edu.agh.hiputs.partition.mapper.util.oneway.OneWayProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.model.WayData;

@Slf4j
@Service
@RequiredArgsConstructor
public class Osm2InternalModelMapperImpl implements Osm2InternalModelMapper{
  private final OneWayProcessor oneWayProcessor;
  private final LanesProcessor lanesProcessor;

  private final List<GraphTransformer> graphTransformers = List.of(
      new LargestCCSelector(),
      new GraphMaxSpeedFiller(),
      new GraphLengthFiller());

  public Graph<JunctionData, WayData> mapToInternalModel(OsmGraph osmGraph) {
    Graph.GraphBuilder<JunctionData, WayData> graphBuilder = new Graph.GraphBuilder<>();
    osmGraph.getNodes().stream().map(this::osmToInternal).forEach(graphBuilder::addNode);
    osmGraph.getWays().stream().flatMap(osmWay -> osmToInternal(osmWay).stream()).forEach(graphBuilder::addEdge);

    Graph<JunctionData, WayData> graph = graphBuilder.build();
    log.info("Building internal graph from osmGraph finished");
    log.info("Applying transforms started");
    for (final GraphTransformer graphTransformer : graphTransformers) {
      graph = graphTransformer.transform(graph);
    }
    log.info("Applying transforms finished");
    return graph;
  }

  private Node<JunctionData, WayData> osmToInternal(OsmNode osmNode) {
    JunctionData junctionData = JunctionData.builder()
        .lat(osmNode.getLatitude())
        .lon(osmNode.getLongitude())
        .tags(getTags(osmNode))
        .build();
    return new Node<>(String.valueOf(osmNode.getId()), junctionData);
  }

  private List<Edge<JunctionData, WayData>> osmToInternal(OsmWay osmWay) {
    List<Edge<JunctionData, WayData>> edges = new LinkedList<>();
    Map<String, String> tags = getTags(osmWay);
    Map<RelativeDirection, List<LaneData>> lanesMap = lanesProcessor.getDataForEachDirectionFromTags(tags);
    boolean isOneway = oneWayProcessor.checkFromTags(tags);

    for (int i = 0; i < osmWay.getNumberOfNodes() - 1; i++) {
      WayData wayData = WayData.builder()
          .tags(tags)
          .isOneWay(isOneway)
          .lanes(lanesMap.get(RelativeDirection.SAME))
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(osmWay.getNodeId(i) + "->" + osmWay.getNodeId(i + 1), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i)),
          JunctionData.builder().isCrossroad(i == 0).build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)),
          JunctionData.builder().isCrossroad(i == osmWay.getNumberOfNodes() - 2).build()));
      edges.add(edge);

      if (isOneway) {
        continue;
      }

      //add opposite lane
      wayData = WayData.builder()
          .tags(tags)
          .isOneWay(false)
          .lanes(lanesMap.get(RelativeDirection.OPPOSITE))
          .build();
      edge = new Edge<>(osmWay.getNodeId(i + 1) + "->" + osmWay.getNodeId(i), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)),
          JunctionData.builder().isCrossroad(i == osmWay.getNumberOfNodes() - 2).build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i)),
          JunctionData.builder().isCrossroad(i == 0).build()));
      edges.add(edge);
    }
    return edges;
  }

  private Map<String, String> getTags(OsmEntity osmEntity) {
    Map<String, String> tags = new HashMap<>();
    for (int i = 0; i < osmEntity.getNumberOfTags(); i++) {
      tags.put(osmEntity.getTag(i).getKey(), osmEntity.getTag(i).getValue());
    }
    return tags;
  }

}
