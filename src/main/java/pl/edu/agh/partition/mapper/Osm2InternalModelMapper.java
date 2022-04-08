package pl.edu.agh.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.partition.model.Edge;
import pl.edu.agh.partition.model.Graph;
import pl.edu.agh.partition.model.Node;
import pl.edu.agh.partition.osm.OsmGraph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Osm2InternalModelMapper {

    private final List<GraphTransformer> graphTransformers = List.of(
            new GraphMaxSpeedFiller(),
            new GraphLengthFiller());

    private Map<Long, Junction> junctionsMap;

    public Graph osmToInternal(OsmGraph osmGraph) {
        Graph graph = new Graph();
        osmGraph.getNodes().stream().map(osmNode -> osmToInternal(osmNode)).forEach(graph::addNode);
        osmGraph.getWays().stream().flatMap(osmWay -> osmToInternal(osmWay).stream()).forEach(graph::addEdge);
        graphTransformers.forEach(t -> t.transform(graph));
        return graph;
    }

    private Node osmToInternal(OsmNode osmNode) {
        Node node = new Node(String.valueOf(osmNode.getId()));
        node.setLat(osmNode.getLatitude());
        node.setLon(osmNode.getLongitude());
        node.setTags(getTags(osmNode));
        return node;
    }

    private List<Edge> osmToInternal(OsmWay osmWay) {
        List<Edge> edges = new LinkedList<>();
        for (int i = 0; i<osmWay.getNumberOfNodes() - 1; i++) {
            Map<String, String> tags = getTags(osmWay);
            Edge edge = new Edge(osmWay.getNodeId(i) + "->" + osmWay.getNodeId(i + 1));
            edge.setSource(new Node(String.valueOf(osmWay.getNodeId(i))));
            edge.setTarget(new Node(String.valueOf(osmWay.getNodeId(i + 1))));
            edge.setTags(tags);
            edges.add(edge);

            if (tags.containsKey("oneway") && tags.get("oneway").equals("true")) {
                edge.setOneWay(true);
                continue;
            }

            //add opposite lane
            edge = new Edge(osmWay.getNodeId(i + 1) + "->" + osmWay.getNodeId(i));
            edge.setSource(new Node(String.valueOf(osmWay.getNodeId(i + 1))));
            edge.setTarget(new Node(String.valueOf(osmWay.getNodeId(i))));
            edge.setTags(tags);
            edges.add(edge);
        }
        return edges;
    }

    private Map<String, String> getTags(OsmEntity osmEntity) {
        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i<osmEntity.getNumberOfTags(); i++) {
            tags.put(osmEntity.getTag(i).getKey(), osmEntity.getTag(i).getValue());
        }
        return tags;
    }

}
