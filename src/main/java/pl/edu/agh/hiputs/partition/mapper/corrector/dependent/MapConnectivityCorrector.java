package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.BridgesCreator;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@RequiredArgsConstructor
public class MapConnectivityCorrector implements Corrector {
  private final List<StronglyConnectedComponent> sCCs;
  private final List<WeaklyConnectedComponent> wCCs;
  private final BridgesCreator bridgesCreator;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    cleanUpSCCsUsingGraph(graph);

    return bridgesCreator.createBetweenCCsOnGraph(sCCs, wCCs, graph);
  }

  private void cleanUpSCCsUsingGraph(Graph<JunctionData, WayData> graph) {
    sCCs.forEach(scc -> scc.getExternalEdgesIds().stream()
        .filter(edgeId -> scc.getNodesIds().contains(graph.getEdges().get(edgeId).getSource().getId()))
        .toList()
        .forEach(scc.getExternalEdgesIds()::remove)
    );
  }
}
