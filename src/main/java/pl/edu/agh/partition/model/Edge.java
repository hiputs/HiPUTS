package pl.edu.agh.partition.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class Edge {

    private final String id;
    private Node source;
    private Node target;
    private double length; // wyliczalny atrybut
    private int maxSpeed;
    private boolean isPriorityRoad;
    private boolean isOneWay;

    private Map<String, String> tags;

    private String patchId;
}
