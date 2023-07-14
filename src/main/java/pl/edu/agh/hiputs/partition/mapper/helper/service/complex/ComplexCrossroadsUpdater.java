package pl.edu.agh.hiputs.partition.mapper.helper.service.complex;

import java.util.Set;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public interface ComplexCrossroadsUpdater {

  void extendWithNodes(Set<Node<JunctionData, WayData>> nodes);

}
