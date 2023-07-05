package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.RestrictionAware;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@RequiredArgsConstructor
public class RestrictionsCorrector implements Corrector {
  private final Set<Restriction> foundRestrictions;
  private final RestrictionAware restrictionAware;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    restrictionAware.provideRestrictions(foundRestrictions.stream()
        .filter(restriction -> isUpToDate(restriction, graph))
        .collect(Collectors.toSet()));

    return graph;
  }

  private boolean isUpToDate(Restriction restriction, Graph<JunctionData, WayData> graph) {
    return graph.getEdges().containsKey(restriction.getFromEdgeId()) &&
        graph.getNodes().containsKey(restriction.getViaNodeId()) &&
        graph.getEdges().containsKey(restriction.getToEdgeId());
  }
}
