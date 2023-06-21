package pl.edu.agh.hiputs.service.pathfinder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.*;
import pl.edu.agh.hiputs.service.pathfinder.astar.ContractionHierarchyAStar;
import pl.edu.agh.hiputs.service.pathfinder.astar.AStarContractionHierarchyPrecomputation;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.concurrent.*;

@Slf4j
public class CHBidirectionalAStar extends CHPathFinder {
    private AStarContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch;

    public CHBidirectionalAStar(MapRepository mapRepository, ThreadPoolExecutor executor) {
        super(mapRepository, executor);
    }

    public CHBidirectionalAStar(MapFragment mapFragment, ThreadPoolExecutor executor) {
        super(mapFragment, executor);
    }

    private AStarContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> createCHBidirectionalAStar(
            Graph<JunctionReadable, LaneReadable> graph,
            ThreadPoolExecutor executor) {
        AStarContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
                new AStarContractionHierarchyPrecomputation<>(graph, executor);

        return precomputation.computeContractionHierarchy();
    }

    @Override
    ShortestPathAlgorithm<JunctionReadable, LaneReadable> initShortestPathAlgorithm(Graph<JunctionReadable, LaneReadable> graph, ThreadPoolExecutor executor) {
        ch = createCHBidirectionalAStar(graph, executor);
        return new ContractionHierarchyAStar<>(ch);
    }
}