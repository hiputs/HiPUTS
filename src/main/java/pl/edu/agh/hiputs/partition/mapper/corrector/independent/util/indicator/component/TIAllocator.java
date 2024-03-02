package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface TIAllocator {

  void allocateAroundNode(Node<JunctionData, WayData> node);
}