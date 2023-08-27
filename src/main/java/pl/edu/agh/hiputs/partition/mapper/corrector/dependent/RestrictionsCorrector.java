package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.RestrictionAware;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@RequiredArgsConstructor
public class RestrictionsCorrector implements Corrector {
  private final Set<Restriction> foundRestrictions;
  private final RestrictionAware restrictionAware;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    Set<Restriction> restrictionsToModify = foundRestrictions.stream()
        .filter(restriction -> restriction.getFromEdgeId().equals(restriction.getToEdgeId()))
        .collect(Collectors.toSet());

    foundRestrictions.removeAll(restrictionsToModify);
    foundRestrictions.addAll(restrictionsToModify.stream()
        .map(restriction -> Restriction.builder()
            .id(restriction.getId())
            .type(restriction.getType())
            .viaNodeId(restriction.getViaNodeId())
            .fromEdgeId(restriction.getFromEdgeId())
            .toEdgeId(findReverseEdge(restriction.getToEdgeId(), graph).orElse(restriction.getToEdgeId()))
            .build())
        .filter(restriction -> !restriction.getFromEdgeId().equals(restriction.getToEdgeId()))
        .collect(Collectors.toSet()));

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

  private Optional<String> findReverseEdge(String edgeId, Graph<JunctionData, WayData> graph) {
    Edge<JunctionData, WayData> processingEdge = graph.getEdges().get(edgeId);
    return processingEdge.getTarget().getOutgoingEdges().stream()
        .filter(outgoingEdge -> outgoingEdge.getTarget().equals(processingEdge.getSource()))
        .findFirst()
        .map(Edge::getId);
  }
}
