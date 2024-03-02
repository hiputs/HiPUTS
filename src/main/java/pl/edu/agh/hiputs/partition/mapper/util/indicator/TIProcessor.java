package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface TIProcessor {

  void checkAndAllocate(Node<JunctionData, WayData> node);
}
