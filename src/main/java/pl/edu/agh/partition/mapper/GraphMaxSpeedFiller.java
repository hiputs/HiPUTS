package pl.edu.agh.partition.mapper;

import pl.edu.agh.partition.model.Edge;
import pl.edu.agh.partition.model.Graph;

import java.util.Map;

class GraphMaxSpeedFiller implements GraphTransformer {

    private static final String MAX_SPEED_TAG = "maxspeed";

    private Graph graph;

    @Override
    public Graph transform(Graph graph) {
//        this.graph = graph;
        for(Edge edge : graph.getEdges().values()) {
            int maxSpeed = calculateMaxSpeed(edge.getTags());
            edge.setMaxSpeed(maxSpeed);
        }
        return graph;
    }

    private int calculateMaxSpeed(Map<String, String> tags) {
        if (tags.containsKey(MAX_SPEED_TAG)) {
            return Integer.parseInt(tags.get(MAX_SPEED_TAG));
        }

        if(tags.containsKey("highway")) {
            switch (tags.get("highway")) {
                case "motorway":
                    return 140;
                case "trunk":
                    return 90;
                case "living_street":
                    return 20;
            }
        }

        //todo should be 50 or 90 - it is hard to parse whether edge is in city area or not :(
        return 50;
    }
}
