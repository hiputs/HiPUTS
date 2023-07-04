package pl.edu.agh.hiputs.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.queue.ServiceQueue;
import pl.edu.agh.hiputs.partition.mapper.helper.service.oneway.OneWayProcessor;
import pl.edu.agh.hiputs.partition.mapper.transformer.GraphTransformer;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;
import pl.edu.agh.hiputs.partition.model.relation.RestrictionType;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.model.WayData;

@Slf4j
@Service
@RequiredArgsConstructor
public class Osm2InternalModelMapperImpl implements Osm2InternalModelMapper{
  private final OneWayProcessor oneWayProcessor;
  private final List<GraphTransformer> graphTransformers;
  private final ServiceQueue<OsmGraph, OsmGraph> filterQueue;
  private final ServiceQueue<Graph<JunctionData, WayData>, Void> detectorQueue;
  private final ServiceQueue<Graph<JunctionData, WayData>, Graph<JunctionData, WayData>> correctorQueue;

  public Graph<JunctionData, WayData> mapToInternalModel(OsmGraph osmGraph) {
    log.info("Filtering osmGraph");
    osmGraph = filterQueue.executeAll(osmGraph);

    log.info("Mapping osmGraph to model graph");
    Graph.GraphBuilder<JunctionData, WayData> graphBuilder = new Graph.GraphBuilder<>();
    osmGraph.getNodes().stream().map(this::osmToInternal).forEach(graphBuilder::addNode);
    osmGraph.getWays().stream().flatMap(osmWay -> osmToInternal(osmWay).stream()).forEach(graphBuilder::addEdge);
    Graph<JunctionData, WayData> graph = graphBuilder.build();

    Map<String, Node<JunctionData, WayData>> nodes = graph.getNodes();
    osmGraph.getRelations().stream()
        .map(this::osmToInternal)
        .filter(this::isComplete)
        .forEach(restriction -> nodes.get(restriction.getViaNodeId()).getData().getRestrictions().add(restriction));

    log.info("Applying transforms");
    for (final GraphTransformer graphTransformer : graphTransformers) {
      graph = graphTransformer.transform(graph);
    }

    log.info("Detecting incorrectness");
    detectorQueue.executeAll(graph);

    log.info("Correcting graph");
    return correctorQueue.executeAll(graph);
  }

  private Node<JunctionData, WayData> osmToInternal(OsmNode osmNode) {
    JunctionData junctionData = JunctionData.builder()
        .lat(osmNode.getLatitude())
        .lon(osmNode.getLongitude())
        .tags(getTags(osmNode))
        .build();
    return new Node<>(String.valueOf(osmNode.getId()), junctionData);
  }

  private List<Edge<JunctionData, WayData>> osmToInternal(OsmWay osmWay) {
    List<Edge<JunctionData, WayData>> edges = new LinkedList<>();
    Map<String, String> tags = getTags(osmWay);
    boolean isOneway = oneWayProcessor.checkFromTags(tags);

    for (int i = 0; i < osmWay.getNumberOfNodes() - 1; i++) {
      WayData wayData = WayData.builder()
          .tags(tags)
          .tagsInOppositeMeaning(false)
          .isOneWay(isOneway)
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(osmWay.getNodeId(i) + "->" + osmWay.getNodeId(i + 1), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i)), JunctionData.builder().build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)), JunctionData.builder().build()));
      edges.add(edge);

      if (isOneway) {
        continue;
      }

      //add opposite lane
      wayData = WayData.builder()
          .tags(tags)
          .tagsInOppositeMeaning(true)
          .isOneWay(false)
          .build();
      edge = new Edge<>(osmWay.getNodeId(i + 1) + "->" + osmWay.getNodeId(i), wayData);
      edge.setSource(new Node<>(String.valueOf(osmWay.getNodeId(i + 1)), JunctionData.builder().build()));
      edge.setTarget(new Node<>(String.valueOf(osmWay.getNodeId(i)), JunctionData.builder().build()));
      edges.add(edge);
    }
    return edges;
  }

  private Restriction osmToInternal(OsmRelation osmRelation) {
    List<OsmRelationMember> members = OsmModelUtil.membersAsList(osmRelation);
    Map<String, String> tags = getTags(osmRelation);

    Restriction.RestrictionBuilder restrictionBuilder = Restriction.builder();

    Optional.of(tags)
        .filter(map -> map.containsKey("restriction"))
        .map(map -> map.get("restriction"))
        .ifPresent(type -> restrictionBuilder.type(RestrictionType.valueOf(type.toUpperCase())));

    members.stream()
        .filter(member -> member.getRole().equals("via"))
        .findFirst()
        .ifPresent(member -> restrictionBuilder.viaNodeId(String.valueOf(member.getId())));

    members.stream()
        .filter(member -> member.getRole().equals("to"))
        .findFirst()
        .ifPresent(member -> restrictionBuilder.toEdgeId(String.valueOf(member.getId())));

    members.stream()
        .filter(member -> member.getRole().equals("from"))
        .findFirst()
        .ifPresent(member -> restrictionBuilder.fromEdgeId(String.valueOf(member.getId())));

    return restrictionBuilder.build();
  }

  private boolean isComplete(Restriction restriction) {
    return StringUtils.isNotBlank(restriction.getId()) &&
        StringUtils.isNotBlank(restriction.getViaNodeId()) &&
        StringUtils.isNotBlank(restriction.getFromEdgeId()) &&
        StringUtils.isNotBlank(restriction.getToEdgeId()) &&
        Objects.nonNull(restriction.getType());
  }

  private Map<String, String> getTags(OsmEntity osmEntity) {
    Map<String, String> tags = new HashMap<>();
    for (int i = 0; i < osmEntity.getNumberOfTags(); i++) {
      tags.put(osmEntity.getTag(i).getKey(), osmEntity.getTag(i).getValue());
    }
    return tags;
  }

}
