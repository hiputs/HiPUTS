package pl.edu.agh.hiputs.partition.mapper.helper.service.complex;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
public class StandardComplexCrossroadsUpdater implements ComplexCrossroadsUpdater {

  private final CrossroadDeterminer crossroadDeterminer;
  private final ComplexCrossroadsRepository complexCrossroadsRepository;
  private final ModelConfigurationService modelConfigService;

  @Override
  public void extendWithNodes(Set<Node<JunctionData, WayData>> nodes) {
    // preparing map for merging crossroads with common occurrences
    Map<String, Set<String>> nodeId2Representatives = complexCrossroadsRepository.getComplexCrossroads()
        .stream()
        .flatMap(cc -> cc.getNodesIdsIn().stream())
        .collect(Collectors.toMap(Function.identity(), nodeId -> new HashSet<>(), (set1, set2) -> new HashSet<>()));

    // merging process
    complexCrossroadsRepository.getComplexCrossroads().forEach(cc -> {
      // this set contains nodes which were added earlier to map and have to be added also to this cc
      Set<String> previousNodesIds = new HashSet<>();

      cc.getNodesIdsIn().forEach(nodeId -> {
        // if this node is contained in another cc, which could be processed earlier,
        // we collect it and representatives from this cc (map should contain them)
        previousNodesIds.addAll(nodeId2Representatives.get(nodeId));

        // just adding all representatives of current cc to all its nodes' sets in map
        Set<String> nodesIdsCurrentlyInMap = new HashSet<>(nodeId2Representatives.get(nodeId));
        nodesIdsCurrentlyInMap.forEach(other -> nodeId2Representatives.get(other).addAll(cc.getNodesIdsIn()));

        // updating also set of processing node in map
        nodeId2Representatives.get(nodeId).addAll(cc.getNodesIdsIn());
      });

      // updating all sets of processed nodes by adding collected nodes, which were processed earlier (during another
      // cc)
      cc.getNodesIdsIn().forEach(nodeId -> nodeId2Representatives.get(nodeId).addAll(previousNodesIds));
    });

    // collecting representatives and their complex crossroads
    Map<String, ComplexCrossroad> nodeId2ComplexCrossroad = nodeId2Representatives.entrySet().stream().map(entry -> {
      ComplexCrossroad complexCrossroad = new ComplexCrossroad();
      entry.getValue().forEach(complexCrossroad::addNode);

      return Pair.of(entry.getKey(), complexCrossroad);
    }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    // retrieving only crossroads from internal nodes
    Set<Node<JunctionData, WayData>> internalCrossroads =
        nodes.stream().filter(crossroadDeterminer::determine).collect(Collectors.toSet());

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
            } else if (crossroadDeterminer.determine(neighbour)) {
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

      getReachableNeighbours(internalCrossroad).stream().filter(internalCrossroads::contains)     // internal neighbours
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
    complexCrossroadsRepository.getComplexCrossroads()
        .addAll(nodeId2ComplexCrossroad.values().stream().distinct().toList());
  }

  private List<Node<JunctionData, WayData>> getReachableNeighbours(Node<JunctionData, WayData> node) {
    return Stream.concat(node.getIncomingEdges()
        .stream()
        .filter(edge -> edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance())
        .map(Edge::getSource), node.getOutgoingEdges()
        .stream()
        .filter(edge -> edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance())
        .map(Edge::getTarget)).toList();
  }

  private void updateMapWithExtendedSet(Map<String, ComplexCrossroad> nodeId2ComplexCrossroad,
      Set<String> extendedNodesIdsSet) {
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
