package pl.edu.agh.hiputs.partition.mapper.util.successor.allocator;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface SuccessorAllocator {

  void allocateOnNode(Node<JunctionData, WayData> node);
}
