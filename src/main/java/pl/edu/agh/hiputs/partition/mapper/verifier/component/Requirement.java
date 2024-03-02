package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface Requirement {

  boolean isSatisfying(Graph<JunctionData, WayData> graph);

  String getName();

}
