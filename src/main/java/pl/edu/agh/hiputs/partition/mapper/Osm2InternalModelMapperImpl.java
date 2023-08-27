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
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.queue.ServiceQueue;
import pl.edu.agh.hiputs.partition.mapper.helper.service.oneway.OneWayProcessor;
import pl.edu.agh.hiputs.partition.mapper.transformer.GraphTransformer;
import pl.edu.agh.hiputs.partition.mapper.verifier.RequirementsVerifier;
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
  private final RequirementsVerifier requirementsVerifier;

  public Graph<JunctionData, WayData> mapToInternalModel(OsmGraph osmGraph) {
    log.info("Filtering osmGraph");
    osmGraph = filterQueue.executeAll(osmGraph);

    log.info("Mapping osmGraph to model graph");
    // preparing
    Map<String, List<Edge<JunctionData, WayData>>> osmWayId2Edges = new HashMap<>();
    Graph.GraphBuilder<JunctionData, WayData> graphBuilder = new Graph.GraphBuilder<>();

    // nodes
    osmGraph.getNodes().stream()
        .map(this::osmToInternal)
        .forEach(graphBuilder::addNode);

    // ways
    osmGraph.getWays().stream()
        .flatMap(osmWay -> flatMapOsmWayToEdges(osmWay, osmWayId2Edges))
        .forEach(graphBuilder::addEdge);

    // building
    Graph<JunctionData, WayData> graph = graphBuilder.build();
    Map<String, Node<JunctionData, WayData>> nodes = graph.getNodes();

    // relations
    osmGraph.getRelations().stream()
        .map(relation -> osmToInternal(relation, osmWayId2Edges))
        .filter(this::isComplete)
        .filter(restriction -> nodes.containsKey(restriction.getViaNodeId()))
        .forEach(restriction -> nodes.get(restriction.getViaNodeId()).getData().getRestrictions().add(restriction));

    log.info("Applying transforms");
    for (final GraphTransformer graphTransformer : graphTransformers) {
      graph = graphTransformer.transform(graph);
    }

    log.info("Detecting incorrectness");
    detectorQueue.executeAll(graph);

    log.info("Correcting graph");
    graph = correctorQueue.executeAll(graph);

    log.info("Verifying requirements on graph");
    requirementsVerifier.verifyAll(graph);

    return graph;
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
      if (osmWay.getNodeId(i) == osmWay.getNodeId(i + 1)) {
        continue;
      }

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

  private Restriction osmToInternal(OsmRelation osmRelation, Map<String, List<Edge<JunctionData, WayData>>> osmWayId2Ends) {
    List<OsmRelationMember> members = OsmModelUtil.membersAsList(osmRelation);
    Map<String, String> tags = getTags(osmRelation);

    Restriction.RestrictionBuilder restrictionBuilder = Restriction.builder();

    // type
    Optional.of(tags)
        .filter(map -> map.containsKey("restriction"))
        .map(map -> map.get("restriction"))
        .ifPresent(type -> {
          try {
            restrictionBuilder.type(RestrictionType.valueOf(type.toUpperCase()));
          } catch(IllegalArgumentException ignored) {}  // no type => filtering out
        });

    // via node
    String viaNodeId = members.stream()
        .filter(member -> member.getRole().equals("via"))
        .findFirst()
        .map(OsmRelationMember::getId)
        .map(String::valueOf)
        .orElse(null);

    if (viaNodeId != null) {
      restrictionBuilder.viaNodeId(viaNodeId);

      // to edge
      members.stream()
          .filter(member -> member.getRole().equals("to"))
          .findFirst()
          .ifPresent(member -> {
            if (osmWayId2Ends.containsKey(String.valueOf(member.getId()))) {
              osmWayId2Ends.get(String.valueOf(member.getId())).stream()
                  .filter(edge -> edge.getSource().getId().equals(viaNodeId))
                  .findFirst()
                  .ifPresent(foundToEdge -> restrictionBuilder.toEdgeId(foundToEdge.getId()));
            }
          });

      // from edge
      members.stream()
          .filter(member -> member.getRole().equals("from"))
          .findFirst()
          .ifPresent(member -> {
            if (osmWayId2Ends.containsKey(String.valueOf(member.getId()))) {
              osmWayId2Ends.get(String.valueOf(member.getId())).stream()
                  .filter(edge -> edge.getTarget().getId().equals(viaNodeId))
                  .findFirst()
                  .ifPresent(foundFromEdge -> restrictionBuilder.fromEdgeId(foundFromEdge.getId()));
            }
          });
    }

    return restrictionBuilder.build();
  }

  private boolean isComplete(Restriction restriction) {
    return StringUtils.isNotBlank(restriction.getId()) &&
        StringUtils.isNotBlank(restriction.getViaNodeId()) &&
        StringUtils.isNotBlank(restriction.getFromEdgeId()) &&
        StringUtils.isNotBlank(restriction.getToEdgeId()) &&
        Objects.nonNull(restriction.getType());
  }

  private Stream<Edge<JunctionData, WayData>> flatMapOsmWayToEdges(
      OsmWay osmWay, Map<String, List<Edge<JunctionData, WayData>>> osmWayId2Edges
  ) {
    List<Edge<JunctionData, WayData>> edges = osmToInternal(osmWay);
    osmWayId2Edges.put(String.valueOf(osmWay.getId()), edges);

    return edges.stream();
  }

  private Map<String, String> getTags(OsmEntity osmEntity) {
    Map<String, String> tags = new HashMap<>();
    for (int i = 0; i < osmEntity.getNumberOfTags(); i++) {
      tags.put(osmEntity.getTag(i).getKey(), osmEntity.getTag(i).getValue());
    }
    return tags;
  }

}
