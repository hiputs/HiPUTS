package pl.edu.agh.partition.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class Node {

    private final String id;
    private Double lon;
    private Double lat;

    @Setter(AccessLevel.NONE)
    private List<Edge> incomingEdges = new LinkedList<>();
    @Setter(AccessLevel.NONE)
    private List<Edge> outgoingEdges = new LinkedList<>();

    private Map<String, String> tags;

    public void merge(Node newNode) {
        incomingEdges.addAll(newNode.getIncomingEdges());
        outgoingEdges.addAll(newNode.getOutgoingEdges());
        tags.putAll(newNode.getTags());
    }

    public void addToIncomingEdges(Edge edge) {
        incomingEdges.add(edge);
    }

    public void addToOutgoingEdges(Edge edge) {
        outgoingEdges.add(edge);
    }

    // To jest przybliżenie - docelowo trzeba sprawdzać geometrie skrzyżowania
    public boolean isCrossroad() {
        return incomingEdges.size() > 2;
    }

    public boolean isBend() {
        return !isCrossroad();
    }
}
