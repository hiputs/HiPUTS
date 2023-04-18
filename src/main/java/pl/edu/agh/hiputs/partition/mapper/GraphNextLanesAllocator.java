package pl.edu.agh.hiputs.partition.mapper;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.successor.SuccessorAllocator;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(7)
@RequiredArgsConstructor
public class GraphNextLanesAllocator implements GraphTransformer {
  private final List<SuccessorAllocator> successorAllocators;

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    successorAllocators.forEach(allocator ->
        graph.getNodes().values().forEach(allocator::allocateOnNode));

    return graph;
  }
}
