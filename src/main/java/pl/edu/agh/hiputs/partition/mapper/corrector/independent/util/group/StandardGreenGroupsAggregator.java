package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.component.GreenGroupRoadsExtractor;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.EdgeSorter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.group.MultipleTIsGreenColorGroup;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@Service
@RequiredArgsConstructor
public class StandardGreenGroupsAggregator implements GreenGroupsAggregator {
  private final GreenGroupRoadsExtractor greenGroupsExtractor;
  private final EdgeSorter edgeSorter;

  @Override
  public void findAndAggregate(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .forEach(this::createGreenColorGroupsForJunction);
  }

  private void createGreenColorGroupsForJunction(Node<JunctionData, WayData> junction) {
    List<Edge<JunctionData, WayData>> sortedIncomingEdges = edgeSorter.getSorted(junction.getIncomingEdges(),
        junction.getIncomingEdges().get(0), edge -> edge.getSource().getData(), edge -> edge.getTarget().getData());

    junction.getData().getSignalsControlCenter().ifPresent(signalsControlCenter ->
        signalsControlCenter.getGreenColorGroups().addAll(
            greenGroupsExtractor.extract(sortedIncomingEdges).stream()
                .map(this::mapEdgesToTIs)
                .map(MultipleTIsGreenColorGroup::new)
                .toList()
        ));
  }

  private List<TrafficIndicatorEditable> mapEdgesToTIs(List<Edge<JunctionData, WayData>> edges) {
    return edges.stream()
        .map(edge -> edge.getData().getTrafficIndicator())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(TrafficIndicatorEditable.class::cast)
        .collect(Collectors.toList());
  }
}
