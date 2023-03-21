package pl.edu.agh.hiputs.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.model.WayData;

@Slf4j
@Service
@RequiredArgsConstructor
public class Osm2InternalModelMapperImpl implements Osm2InternalModelMapper{

  private final CrossroadFinder crossroadsFinder;

  private final List<GraphTransformer> graphTransformers = List.of(
      new LargestCCSelector(),
      new GraphMaxSpeedFiller(),
      new GraphLengthFiller());

  public Graph<JunctionData, WayData> mapToInternalModel(OsmGraph osmGraph) {
    Graph.GraphBuilder<JunctionData, WayData> graphBuilder = new Graph.GraphBuilder<>();
    osmGraph.getNodes().stream().map(this::osmToInternal).forEach(graphBuilder::addNode);

    Set<Long> foundCrossroads = crossroadsFinder.findAll(osmGraph.getWays());
    osmGraph.getWays().stream().flatMap(osmWay -> osmToInternal(osmWay, foundCrossroads).stream()).forEach(graphBuilder::addEdge);

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

  private List<Edge<JunctionData, WayData>> osmToInternal(OsmWay osmWay, Set<Long> crossroads) {
    List<Edge<JunctionData, WayData>> edges = new LinkedList<>();
    for (int i = 0; i < osmWay.getNumberOfNodes() - 1; i++) {
      Map<String, String> tags = getTags(osmWay);
      WayData wayData = WayData.builder()
          .tags(tags)
          .isOneWay(tags.containsKey("oneway") && tags.get("oneway").equals("yes")
              || tags.containsKey("junction") && tags.get("junction").equals("roundabout"))
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(osmWay.getNodeId(i) + "->" + osmWay.getNodeId(i + 1), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i)),
          JunctionData.builder().isCrossroad(crossroads.contains(osmWay.getNodeId(i))).build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)),
          JunctionData.builder().isCrossroad(crossroads.contains(osmWay.getNodeId(i + 1))).build()));
      edges.add(edge);

      if (wayData.isOneWay()) {
        continue;
      }

      //add opposite lane
      wayData = WayData.builder()
          .tags(tags)
          .isOneWay(false)
          .build();
      edge = new Edge<>(osmWay.getNodeId(i + 1) + "->" + osmWay.getNodeId(i), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)), JunctionData.builder().isCrossroad(crossroads.contains(osmWay.getNodeId(i + 1))).build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i)), JunctionData.builder().isCrossroad(crossroads.contains(osmWay.getNodeId(i))).build()));
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
