package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.lanes.LanesCreator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.SuccessorAllocator;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(1)
@RequiredArgsConstructor
public class LanesCorrector implements Corrector {

  private final LanesCreator lanesCreator;
  private final List<SuccessorAllocator> successorAllocators;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    graph.getEdges().values().forEach(lanesCreator::deduceAndCreate);

    successorAllocators.forEach(allocator -> graph.getNodes().values().forEach(allocator::allocateOnNode));

    return graph;
  }
}
