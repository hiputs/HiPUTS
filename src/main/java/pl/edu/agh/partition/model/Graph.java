package pl.edu.agh.partition.model;


import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Graph {

    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();

    public void addNode(Node newNode) {
        if (!edges.isEmpty()) {
            throw new RuntimeException("Cannot add new when graph contains edges");
        }

        String newNodeId = newNode.getId();
        if(nodes.containsKey(newNodeId)) {
            nodes.get(newNodeId).merge(newNode);
        } else {
            nodes.put(newNodeId, newNode);
        }
    }

    public void addEdge(Edge edge) {
        Node source = nodes.get(edge.getSource().getId());
        edge.setSource(source);
        source.addToOutgoingEdges(edge);

        Node target = nodes.get(edge.getTarget().getId());
        edge.setTarget(target);
        target.addToIncomingEdges(edge);

        edges.put(edge.getId(), edge);
    }
}
