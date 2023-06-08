package pl.edu.agh.hiputs.service.pathfinder.astar;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

public class AStarHeuristicForJunction implements AStarAdmissibleHeuristic<JunctionReadable> {
    @Override
    public double getCostEstimate(JunctionReadable source, JunctionReadable sink) {
        if (source.getLatitude() == null || source.getLongitude() == null || sink.getLongitude() == null || sink.getLatitude() == null) {
            return 0.0;
        }
        double latitudeDiff = source.getLatitude() - sink.getLatitude();
        double longitudeDiff = source.getLongitude() - sink.getLongitude();
        return Math.sqrt(latitudeDiff*latitudeDiff + longitudeDiff*latitudeDiff);
    }

    @Override
    public <E> boolean isConsistent(Graph<JunctionReadable, E> graph) {
        return AStarAdmissibleHeuristic.super.isConsistent(graph);
    }
}
