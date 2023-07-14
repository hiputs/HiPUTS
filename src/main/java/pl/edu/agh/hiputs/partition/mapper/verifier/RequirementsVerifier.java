package pl.edu.agh.hiputs.partition.mapper.verifier;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface RequirementsVerifier {

  void verifyAll(Graph<JunctionData, WayData> graph);

}
