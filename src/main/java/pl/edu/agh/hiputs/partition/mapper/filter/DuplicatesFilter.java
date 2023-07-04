package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@Service
@Order(4)
public class DuplicatesFilter implements Filter{

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {
    Collection<OsmNode> processingNodes = collectOrMergeNodesByLocation(osmGraph.getNodes());
    Collection<OsmWay> processingWays = collectOrMergeWaysByLocation(osmGraph.getWays());

    processingNodes = collectOrMergeNodesById(processingNodes);
    processingWays = collectOrDeleteWaysById(processingWays);

    // count nodes occurrences
    Map<Long, Long> nodeId2Occurrences = processingWays.stream()
        .flatMap(osmWay -> Arrays.stream(OsmModelUtil.nodesAsList(osmWay).toArray()).boxed())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    // removing all single nodes (without ways)
    processingNodes = processingNodes.stream()
        .filter(osmNode -> nodeId2Occurrences.containsKey(osmNode.getId()))
        .collect(Collectors.toList());

    return new OsmGraph(processingNodes.stream().toList(), processingWays.stream().toList(), osmGraph.getRelations());
  }

  private Collection<OsmNode> collectOrMergeNodesByLocation(Collection<OsmNode> nodes) {
    Map<NodeLocationWrapper, OsmNode> nodesByLocationAggregator = new HashMap<>();

    nodes.forEach(nextNode -> {
      NodeLocationWrapper nodeLocationKey = new NodeLocationWrapper(nextNode.getLongitude(), nextNode.getLatitude());
      if (nodesByLocationAggregator.containsKey(nodeLocationKey)) {
        // conflict => merge nodes and their contents with age priority for older ones
        nodesByLocationAggregator.put(nodeLocationKey, mergeNodes(nodesByLocationAggregator.get(nodeLocationKey), nextNode));
      } else {
        nodesByLocationAggregator.put(nodeLocationKey, nextNode);
      }
    });

    return nodesByLocationAggregator.values();
  }

  private Collection<OsmWay> collectOrMergeWaysByLocation(Collection<OsmWay> ways) {
    Map<List<Long>, OsmWay> waysByNodesAggregator = new HashMap<>();

    ways.forEach(nextWay -> {
      List<Long> nextWayNodes = Arrays.stream(OsmModelUtil.nodesAsList(nextWay).toArray()).boxed().toList();
      if (waysByNodesAggregator.containsKey(nextWayNodes)) {
        // conflict => merge ways and their contents with age priority for older ones
        waysByNodesAggregator.put(nextWayNodes, mergeWays(waysByNodesAggregator.get(nextWayNodes), nextWay));
      } else {
        waysByNodesAggregator.put(nextWayNodes, nextWay);
      }
    });

    return waysByNodesAggregator.values();
  }

  private Collection<OsmNode> collectOrMergeNodesById(Collection<OsmNode> nodes) {
    Map<Long, OsmNode> nodesByIdAggregator = new HashMap<>();

    nodes.forEach(nextNode -> {
      if (nodesByIdAggregator.containsKey(nextNode.getId())) {
        // conflict => delete newer node with the same id
        nodesByIdAggregator.put(nextNode.getId(), mergeNodes(nodesByIdAggregator.get(nextNode.getId()), nextNode));
      } else {
        nodesByIdAggregator.put(nextNode.getId(), nextNode);
      }
    });

    return nodesByIdAggregator.values();
  }

  private Collection<OsmWay> collectOrDeleteWaysById(Collection<OsmWay> ways) {
    Map<Long, OsmWay> waysByIdAggregator = new HashMap<>();

    ways.forEach(nextWay -> {
      if (waysByIdAggregator.containsKey(nextWay.getId())) {
        // conflict => save older way
        if (nextWay.getMetadata().getTimestamp() < waysByIdAggregator.get(nextWay.getId()).getMetadata().getTimestamp()) {
          // proposed is older so replacing
          waysByIdAggregator.put(nextWay.getId(), nextWay);
        }
      } else {
        waysByIdAggregator.put(nextWay.getId(), nextWay);
      }
    });

    return waysByIdAggregator.values();
  }

  private OsmNode mergeNodes(OsmNode first, OsmNode second) {
    return first.getMetadata().getTimestamp() < second.getMetadata().getTimestamp() ?
        new Node(first.getId(), first.getLongitude(), first.getLatitude(), mergeTags(first, second), first.getMetadata()) :
        new Node(second.getId(), second.getLongitude(), second.getLatitude(), mergeTags(second, first), second.getMetadata());
  }

  private OsmWay mergeWays(OsmWay first, OsmWay second) {
    return first.getMetadata().getTimestamp() < second.getMetadata().getTimestamp() ?
        new Way(first.getId(), OsmModelUtil.nodesAsList(first), mergeTags(first, second), first.getMetadata()) :
        new Way(second.getId(), OsmModelUtil.nodesAsList(second), mergeTags(second, first), second.getMetadata());
  }

  private <O extends OsmEntity> List<? extends OsmTag> mergeTags(O olderEntity, O newerEntity) {
    Map<String, String> olderTags = OsmModelUtil.getTagsAsMap(olderEntity);
    Map<String, String> newerTags = OsmModelUtil.getTagsAsMap(newerEntity);

    newerTags.forEach((key, value) -> olderTags.merge(
        key, value, (v1, v2) -> v1
    ));

    return olderTags.entrySet().stream()
        .map(entry -> new Tag(entry.getKey(), entry.getValue()))
        .toList();
  }

  @EqualsAndHashCode
  @RequiredArgsConstructor
  private static class NodeLocationWrapper {
    private final double longitude;
    private final double latitude;
  }
}
