package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity.CCFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(6)
@RequiredArgsConstructor
public class ConnectionRequirement implements Requirement {

  private final CCFinder<StronglyConnectedComponent> sccFinder;
  private final CCFinder<WeaklyConnectedComponent> wccFinder;

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return sccFinder.lookup(graph).size() == 1 || wccFinder.lookup(graph).size() == 1;
  }

  @Override
  public String getName() {
    return "6. Graph is weakly or/and strongly connected.";
  }
}
