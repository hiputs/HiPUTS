package pl.edu.agh.hiputs.server.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Edge;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;
import pl.edu.agh.hiputs.server.partition.model.graph.Node;
import pl.edu.agh.hiputs.server.partition.osm.OsmGraph;

public class Osm2InternalModelMapper {

  private final List<GraphTransformer> graphTransformers = List.of(new GraphMaxSpeedFiller(), new GraphLengthFiller());

  public Graph<JunctionData, WayData> osmToInternal(OsmGraph osmGraph) {
    Graph<JunctionData, WayData> graph = new Graph<>();
    osmGraph.getNodes().stream().map(osmNode -> osmToInternal(osmNode)).forEach(graph::addNode);
    osmGraph.getWays().stream().flatMap(osmWay -> osmToInternal(osmWay).stream()).forEach(graph::addEdge);
    graphTransformers.forEach(t -> t.transform(graph));
    return graph;
  }

  private Node<JunctionData, WayData> osmToInternal(OsmNode osmNode) {
    Node<JunctionData, WayData> node = new Node<>(String.valueOf(osmNode.getId()));
    node.getData().setLat(osmNode.getLatitude());
    node.getData().setLon(osmNode.getLongitude());
    node.getData().setTags(getTags(osmNode));
    return node;
  }

  private List<Edge<JunctionData, WayData>> osmToInternal(OsmWay osmWay) {
    List<Edge<JunctionData, WayData>> edges = new LinkedList<>();
    for (int i = 0; i < osmWay.getNumberOfNodes() - 1; i++) {
      Map<String, String> tags = getTags(osmWay);
      Edge<JunctionData, WayData> edge = new Edge<>(osmWay.getNodeId(i) + "->" + osmWay.getNodeId(i + 1));
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i))));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i + 1))));
      edge.getData().setTags(tags);
      edges.add(edge);

      if (tags.containsKey("oneway") && tags.get("oneway").equals("true")) {
        edge.getData().setOneWay(true);
        continue;
      }

      //add opposite lane
      edge = new Edge<>(osmWay.getNodeId(i + 1) + "->" + osmWay.getNodeId(i));
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i + 1))));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i))));
      edge.getData().setTags(tags);
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
