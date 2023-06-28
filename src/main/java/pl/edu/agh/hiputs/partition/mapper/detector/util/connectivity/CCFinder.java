package pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.ConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface CCFinder<C extends ConnectedComponent> {

  List<C> lookup(Graph<JunctionData, WayData> graph);

}
