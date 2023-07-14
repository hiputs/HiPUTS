package pl.edu.agh.hiputs.partition.mapper.helper.service.complex;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.CrossroadDeterminer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepository;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class StandardComplexCrossroadsUpdater implements ComplexCrossroadsUpdater{
  private final CrossroadDeterminer crossroadDeterminer;
  private final ComplexCrossroadsRepository complexCrossroadsRepository;
  private final ModelConfigurationService modelConfigService;

  @Override
  public void extendWithNodes(Set<Node<JunctionData, WayData>> nodes) {
    // collecting representatives and their complex crossroads
    Map<String, ComplexCrossroad> nodeId2ComplexCrossroad = complexCrossroadsRepository.getComplexCrossroads().stream()
        .flatMap(cc -> cc.getNodesIdsIn().stream()
            .map(nodeId -> Pair.of(nodeId, cc)))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    // retrieving only crossroads from internal nodes
    Set<Node<JunctionData, WayData>> internalCrossroads = nodes.stream()
        .filter(crossroadDeterminer::determine)
        .collect(Collectors.toSet());

    // first iteration is designated for external neighbours lookup
    internalCrossroads.forEach(internalCrossroad -> {
      Set<String> extendedNodesIdsSet = new HashSet<>();

      if (nodeId2ComplexCrossroad.containsKey(internalCrossroad.getId())) {
        // if processed internal crossroad is already in complex crossroad, then add all their friends
        extendedNodesIdsSet.addAll(nodeId2ComplexCrossroad.get(internalCrossroad.getId()).getNodesIdsIn());
      } else {
        // if not, just add only this node
        extendedNodesIdsSet.add(internalCrossroad.getId());
      }

      getReachableNeighbours(internalCrossroad).stream()
          .filter(neighbour -> !internalCrossroads.contains(neighbour)) // external neighbours
          .forEach(neighbour -> {
            if (nodeId2ComplexCrossroad.containsKey(neighbour.getId())) {
              // if neighbour is already in complex crossroad, then add all their friends
              extendedNodesIdsSet.addAll(nodeId2ComplexCrossroad.get(neighbour.getId()).getNodesIdsIn());
            } else if(crossroadDeterminer.determine(neighbour)) {
              // if not, just add only neighbour
              extendedNodesIdsSet.add(neighbour.getId());
            }
          });

      // build new extended crossroad only if this is not a single internal
      if (extendedNodesIdsSet.size() > 1) {
        updateMapWithExtendedSet(nodeId2ComplexCrossroad, extendedNodesIdsSet);
      }
    });

    // second iteration is designated for internal nodes only
    internalCrossroads.forEach(internalCrossroad -> {
      Set<String> extendedNodesIdsSet = new HashSet<>();

      if (nodeId2ComplexCrossroad.containsKey(internalCrossroad.getId())) {
        // if processed internal crossroad is already in complex crossroad, then add all their friends
        extendedNodesIdsSet.addAll(nodeId2ComplexCrossroad.get(internalCrossroad.getId()).getNodesIdsIn());
      } else {
        // if not, just add only this node
        extendedNodesIdsSet.add(internalCrossroad.getId());
      }

      getReachableNeighbours(internalCrossroad).stream()
          .filter(internalCrossroads::contains)     // internal neighbours
          .forEach(neighbour -> {
            if (nodeId2ComplexCrossroad.containsKey(neighbour.getId())) {
              // if neighbour is already in complex crossroad, then add all their friends
              extendedNodesIdsSet.addAll(nodeId2ComplexCrossroad.get(neighbour.getId()).getNodesIdsIn());
            } else {
              // if not, just add only neighbour
              extendedNodesIdsSet.add(neighbour.getId());
            }
          });

      // build new extended crossroad only if this is not a single internal
      if (extendedNodesIdsSet.size() > 1) {
        updateMapWithExtendedSet(nodeId2ComplexCrossroad, extendedNodesIdsSet);
      }
    });

    // finally update repository
    complexCrossroadsRepository.getComplexCrossroads().clear();
    complexCrossroadsRepository.getComplexCrossroads().addAll(
        nodeId2ComplexCrossroad.values().stream().distinct().toList());
  }

  private List<Node<JunctionData, WayData>> getReachableNeighbours(Node<JunctionData, WayData> node) {
    return Stream.concat(
        node.getIncomingEdges().stream()
            .filter(edge -> edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance())
            .map(Edge::getSource),
        node.getOutgoingEdges().stream()
            .filter(edge -> edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance())
            .map(Edge::getTarget))
        .toList();
  }

  private void updateMapWithExtendedSet(
      Map<String, ComplexCrossroad> nodeId2ComplexCrossroad, Set<String> extendedNodesIdsSet
  ) {
    extendedNodesIdsSet.forEach(nodeId -> {
      if (nodeId2ComplexCrossroad.containsKey(nodeId)) {
        ComplexCrossroad foundComplexCrossroad = nodeId2ComplexCrossroad.get(nodeId);
        extendedNodesIdsSet.forEach(foundComplexCrossroad::addNode);
      } else {
        ComplexCrossroad newComplexCrossroad = new ComplexCrossroad();
        extendedNodesIdsSet.forEach(newComplexCrossroad::addNode);

        nodeId2ComplexCrossroad.put(nodeId, newComplexCrossroad);
      }
    });
  }
}
