package pl.edu.agh.hiputs.service.pathfinder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.*;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.concurrent.*;

@Slf4j
public class CHBidirectionalDijkstra extends CHPathFinder {
    private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch;

    public CHBidirectionalDijkstra(MapRepository mapRepository, ThreadPoolExecutor executor) {
        super(mapRepository, executor);
    }

    public CHBidirectionalDijkstra(MapFragment mapFragment, ThreadPoolExecutor executor) {
        super(mapFragment, executor);
    }

    private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> createCHBidirectionalDijkstra(
            Graph<JunctionReadable, LaneReadable> graph,
            ThreadPoolExecutor executor) {
        ContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        return precomputation.computeContractionHierarchy();
    }

    @Override
    ShortestPathAlgorithm<JunctionReadable, LaneReadable> initShortestPathAlgorithm(Graph<JunctionReadable, LaneReadable> graph, ThreadPoolExecutor executor) {
        ch = createCHBidirectionalDijkstra(graph, executor);
        return new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }
}
