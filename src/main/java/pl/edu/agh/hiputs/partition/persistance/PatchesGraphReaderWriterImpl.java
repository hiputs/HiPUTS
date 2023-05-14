package pl.edu.agh.hiputs.partition.persistance;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupEditable;
import pl.edu.agh.hiputs.partition.model.lights.group.GreenColorGroupReadable;
import pl.edu.agh.hiputs.partition.model.lights.group.MultipleTIsGreenColorGroup;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

@Slf4j
@Service
public class PatchesGraphReaderWriterImpl implements PatchesGraphReader, PatchesGraphWriter {

  private static final String COLLECTION_ELEMENT_CSV_DELIMITER = ":ELEM:";
  private static final String MAP_KEY_VALUE_PAIR_CSV_DELIMITER = ":PAIR:";

  @Override
  public void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph, Path exportPath) {
    try {
      saveGraphWithPatches(graph, ExportDescriptor.builder().exportDirAbsolutePath(exportPath.toAbsolutePath().toString()).build());
    } catch (IOException e) {
      log.error("Error occurred while saving graph with patches: " + e.getMessage());
    }
  }

  @Override
  public Graph<PatchData, PatchConnectionData> readGraphWithPatches(Path importPath) {
    try {
      Graph<PatchData, PatchConnectionData> graph = readGraphWithPatches(
          ExportDescriptor.builder().exportDirAbsolutePath(importPath.toAbsolutePath().toString()).build());
      return graph;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph, ExportDescriptor exportDescriptor)
      throws IOException {
    FileWriter nodesWriter = new FileWriter(exportDescriptor.getNodesFilePath());
    FileWriter edgesWriter = new FileWriter(exportDescriptor.getEdgesFilePath());
    FileWriter lanesWriter = new FileWriter(exportDescriptor.getLanesFilePath());
    FileWriter patchesWriter = new FileWriter(exportDescriptor.getPatchesFilePath());
    FileWriter signalGroupsWriter = new FileWriter(exportDescriptor.getSignalGroupsFilePath());
    FileWriter signalCentersWriter = new FileWriter(exportDescriptor.getSignalCentersFilePath());

    try (CSVPrinter nodesPrinter = new CSVPrinter(nodesWriter,
        CSVFormat.DEFAULT.builder().setHeader(NodeHeaders.class).build());
        CSVPrinter edgesPrinter = new CSVPrinter(edgesWriter,
            CSVFormat.DEFAULT.builder().setHeader(EdgeHeader.class).build());
        CSVPrinter lanesPrinter = new CSVPrinter(lanesWriter,
            CSVFormat.DEFAULT.builder().setHeader(LaneHeader.class).build());
        CSVPrinter patchesPrinter = new CSVPrinter(patchesWriter,
            CSVFormat.DEFAULT.builder().setHeader(PatchHeader.class).build());
        CSVPrinter signalGroupsPrinter = new CSVPrinter(signalGroupsWriter,
            CSVFormat.DEFAULT.builder().setHeader(SignalGroupHeader.class).build());
        CSVPrinter signalCentersPrinter = new CSVPrinter(signalCentersWriter,
            CSVFormat.DEFAULT.builder().setHeader(SignalCenterHeader.class).build())) {
      for (Node<PatchData, PatchConnectionData> p : graph.getNodes().values()) {
        patchesPrinter.printRecord(p.getId(),
            mapToCsv(p.getOutgoingEdges().stream().map(e -> e.getTarget().getId()).collect(Collectors.toList())));

        for (Node<JunctionData, WayData> n : p.getData().getGraphInsidePatch().getNodes().values()) {
          if (Objects.equals(n.getData().getPatchId(), p.getId())) {
            nodesPrinter.printRecord(
                n.getId(),
                n.getData().getLon(),
                n.getData().getLat(),
                n.getData().isCrossroad(),
                n.getData().getPatchId(),
                n.getData().getSignalsControlCenter().map(SignalsControlCenter::getId).orElse(null),
                mapToCsv(n.getData().getTags()));
          }
        }

        for (Edge<JunctionData, WayData> e : p.getData().getGraphInsidePatch().getEdges().values()) {
          edgesPrinter.printRecord(
              e.getSource().getId(),
              e.getTarget().getId(),
              e.getData().getLength(),
              e.getData().getMaxSpeed(),
              e.getData().isPriorityRoad(),
              e.getData().isOneWay(),
              e.getData().getPatchId(),
              e.getData().getTrafficIndicator().map(TrafficIndicatorReadable::getId).orElse(null),
              mapToCsv(e.getData().getLanes().stream()
                  .map(LaneData::getId)
                  .toList()),
              mapToCsv(e.getData().getTags()));
        }

        // taking all lanes without repeats
        List<LaneData> distinctLanes = p.getData().getGraphInsidePatch().getEdges().values().stream()
            .flatMap(edge -> edge.getData().getLanes().stream())
            .distinct()
            .toList();

        // saving distinct lanes with successors as IDs to one file
        for (LaneData laneData : distinctLanes) {
          lanesPrinter.printRecord(
              laneData.getId(),
              mapToCsv(laneData.getAvailableSuccessors().stream()
                  .map(LaneData::getId)
                  .toList())
          );
        }

        // taking all signal control centers from crossroads
        List<SignalsControlCenter> distinctSignalCenters = p.getData().getGraphInsidePatch().getNodes().values().stream()
            .filter(node -> node.getData().getSignalsControlCenter().isPresent())
            .map(node -> node.getData().getSignalsControlCenter().get())
            .distinct()
            .toList();

        // saving distinct signal controls with green groups as IDs to one file
        for (SignalsControlCenter controlCenter : distinctSignalCenters) {
          signalCentersPrinter.printRecord(
              controlCenter.getId(),
              controlCenter.getDurationTime(),
              mapToCsv(controlCenter.getGreenColorGroups().stream()
                  .map(GreenColorGroupEditable::getId)
                  .toList())
          );
        }

        // taking all signal groups from signal control centers
        List<GreenColorGroupEditable> distinctSignalGroups = distinctSignalCenters.stream()
            .flatMap(signalsControlCenter -> signalsControlCenter.getGreenColorGroups().stream())
            .distinct()
            .toList();

        // saving distinct signal groups with traffic indicators as IDs to one file
        for (GreenColorGroupReadable colorGroup : distinctSignalGroups) {
          signalGroupsPrinter.printRecord(
              colorGroup.getId(),
              mapToCsv(colorGroup.getTrafficIndicators().stream()
                  .map(TrafficIndicatorReadable::getId)
                  .toList())
          );
        }
      }
    }
  }

  private Graph<PatchData, PatchConnectionData> readGraphWithPatches(ExportDescriptor exportDescriptor)
      throws IOException {
    FileReader nodesReader = new FileReader(exportDescriptor.getNodesFilePath());
    FileReader edgesReader = new FileReader(exportDescriptor.getEdgesFilePath());
    FileReader lanesReader = new FileReader(exportDescriptor.getLanesFilePath());
    FileReader patchesReader = new FileReader(exportDescriptor.getPatchesFilePath());
    FileReader signalGroupsReader = new FileReader(exportDescriptor.getSignalGroupsFilePath());
    FileReader signalCentersReader = new FileReader(exportDescriptor.getSignalCentersFilePath());

    Graph.GraphBuilder<JunctionData, WayData> wholeMapGraph = new GraphBuilder<>();
    Map<String, Graph.GraphBuilder<JunctionData, WayData>> insideGraphs = new HashMap<>();

    Map<String, Node<JunctionData, WayData>> nodeId2Node = new HashMap<>();

    // retrieving signal green groups
    Iterable<CSVRecord> records =
        CSVFormat.DEFAULT.builder().setHeader(SignalGroupHeader.class)
            .setSkipHeaderRecord(true).build().parse(signalGroupsReader);
    Map<String, Collection<String>> groupsAsIDs = new HashMap<>();
    for (CSVRecord record : records) {
      groupsAsIDs.put(
          record.get(SignalGroupHeader.id),
          csvToCollection(record.get(SignalGroupHeader.trafficIndicators))
      );
    }

    // extracting traffic indicators
    Map<String, TrafficIndicator> idToTrafficIndicator = groupsAsIDs.values().stream()
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toMap(Function.identity(), TrafficIndicator::new));

    // building group objects
    Map<String, MultipleTIsGreenColorGroup> idToGreenColorGroup = groupsAsIDs.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> new MultipleTIsGreenColorGroup(entry.getKey(),
                entry.getValue().stream()
                    .map(idToTrafficIndicator::get)
                    .collect(Collectors.toList())
            )));

    // retrieving signal control centers
    records =
        CSVFormat.DEFAULT.builder().setHeader(SignalCenterHeader.class)
            .setSkipHeaderRecord(true).build().parse(signalCentersReader);
    Map<String, Pair<String, Collection<String>>> centersAsIDs = new HashMap<>();
    for (CSVRecord record : records) {
      centersAsIDs.put(
          record.get(SignalCenterHeader.id),
          Pair.of(record.get(SignalCenterHeader.durationTime), csvToCollection(record.get(SignalCenterHeader.greenColorGroups)))
      );
    }

    // building control center objects
    Map<String, StandardSignalsControlCenter> idToControlCenter = centersAsIDs.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> new StandardSignalsControlCenter(
                entry.getKey(),
                Optional.ofNullable(entry.getValue().getLeft())
                    .map(Integer::parseInt)
                    .orElse(0),
                entry.getValue().getRight().stream()
                    .map(idToGreenColorGroup::get)
                    .collect(Collectors.toList())
            )));

    records =
        CSVFormat.DEFAULT.builder().setHeader(NodeHeaders.class).setSkipHeaderRecord(true).build().parse(nodesReader);

    for (CSVRecord record : records) {
      JunctionData junctionData = JunctionData.builder()
          .lon(Double.parseDouble(record.get(NodeHeaders.longitude)))
          .lat(Double.parseDouble(record.get(NodeHeaders.latitude)))
          .isCrossroad(Boolean.parseBoolean(record.get(NodeHeaders.is_crossroad)))
          .patchId(record.get(NodeHeaders.patch_id))
          .signalsControlCenter(Strings.isBlank(record.get(NodeHeaders.signalsControlCenter)) ?
              Optional.empty() : Optional.of(idToControlCenter.get(record.get(NodeHeaders.signalsControlCenter))))
          .tags(csvToMap(record.get(NodeHeaders.tags)))
          .build();

      if (!insideGraphs.containsKey(record.get(NodeHeaders.patch_id))) {
        insideGraphs.put(record.get(NodeHeaders.patch_id), new GraphBuilder<>());
      }
      Node<JunctionData, WayData> newNode = new Node<>(record.get(NodeHeaders.id), junctionData);
      nodeId2Node.put(record.get(NodeHeaders.id), newNode);
      insideGraphs.get(record.get(NodeHeaders.patch_id)).addNode(newNode);
      wholeMapGraph.addNode(newNode);
    }

    // parsing lane info as map ID -> [successors IDs]
    records =
        CSVFormat.DEFAULT.builder().setHeader(LaneHeader.class).setSkipHeaderRecord(true).build().parse(lanesReader);
    Map<String, Collection<String>> lanesAsIds = new HashMap<>();
    for (CSVRecord record : records) {
      lanesAsIds.put(
          record.get(LaneHeader.id),
          csvToCollection(record.get(LaneHeader.availableSuccessors))
      );
    }

    // mapping lane ID to LaneData
    Map<String, LaneData> laneIdToLaneData = lanesAsIds.keySet().stream()
        .map(laneId -> LaneData.builder().id(laneId).build())
        .collect(Collectors.toMap(LaneData::getId, Function.identity()));

    // assigning successors to each LaneData using previously created map of IDs
    laneIdToLaneData.values().forEach(laneData -> laneData.getAvailableSuccessors().addAll(
        lanesAsIds.get(laneData.getId()).stream()
            .map(laneIdToLaneData::get)
            .toList()
    ));

    records =
        CSVFormat.DEFAULT.builder().setHeader(EdgeHeader.class).setSkipHeaderRecord(true).build().parse(edgesReader);
    for (CSVRecord record : records) {
      WayData wayData = WayData.builder()
          .length(Double.parseDouble(record.get(EdgeHeader.length)))
          .maxSpeed(Integer.parseInt(record.get(EdgeHeader.max_speed)))
          .isPriorityRoad(Boolean.parseBoolean(record.get(EdgeHeader.is_priority_road)))
          .isOneWay(Boolean.parseBoolean(record.get(EdgeHeader.is_one_way)))
          .patchId(record.get(EdgeHeader.patch_id))
          .trafficIndicator(Strings.isBlank(record.get(EdgeHeader.trafficIndicator)) ?
              Optional.empty() : Optional.of(idToTrafficIndicator.get(record.get(EdgeHeader.trafficIndicator))))
          .lanes(csvToCollection(record.get(EdgeHeader.lanes)).stream()
              .map(laneIdToLaneData::get)
              .toList())
          .tags(csvToMap(record.get(EdgeHeader.tags)))
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(record.get(EdgeHeader.source) + "->" + record.get(EdgeHeader.target), wayData);
      edge.setSource(nodeId2Node.get(record.get(EdgeHeader.source)));
      edge.setTarget(nodeId2Node.get(record.get(EdgeHeader.target)));
      if (!insideGraphs.containsKey(record.get(EdgeHeader.patch_id))) {
        insideGraphs.put(record.get(EdgeHeader.patch_id), new GraphBuilder<>());
      }
      insideGraphs.get(record.get(EdgeHeader.patch_id)).addEdge(edge);
      wholeMapGraph.addEdge(edge);
    }

    Graph.GraphBuilder<PatchData, PatchConnectionData> resultGraph = new GraphBuilder<>();
    for (String patchId : insideGraphs.keySet()) {
      PatchData patchData = PatchData.builder().graphInsidePatch(insideGraphs.get(patchId).build()).build();
      resultGraph.addNode(new Node<>(patchId, patchData));
    }

    records =
        CSVFormat.DEFAULT.builder().setHeader(PatchHeader.class).setSkipHeaderRecord(true).build().parse(patchesReader);
    for (CSVRecord record : records) {
      record.get("id");
      for (String neighbouringPatchId : csvToCollection(record.get(PatchHeader.neighbouring_patches_ids))) {
        Edge<PatchData, PatchConnectionData> edge = new Edge<>(record.get(PatchHeader.id) + "->" + neighbouringPatchId, null);
        edge.setSource(new Node<>(record.get(PatchHeader.id), null));
        edge.setTarget(new Node<>(neighbouringPatchId, null));
        resultGraph.addEdge(edge);
      }
    }

    return resultGraph.build();
  }

  private String mapToCsv(Map<String, String> map) {
    return map.entrySet()
        .stream()
        .map(entry -> entry.getKey() + MAP_KEY_VALUE_PAIR_CSV_DELIMITER + entry.getValue())
        .collect(Collectors.joining(COLLECTION_ELEMENT_CSV_DELIMITER));
  }

  private Map<String, String> csvToMap(String csvRepr) {
    if (Strings.isBlank(csvRepr)) {
      return new HashMap<>();
    }
    return Arrays.stream(csvRepr.split(COLLECTION_ELEMENT_CSV_DELIMITER))
        .map(e -> Pair.of(e.split(MAP_KEY_VALUE_PAIR_CSV_DELIMITER)[0], e.split(MAP_KEY_VALUE_PAIR_CSV_DELIMITER)[1]))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private String mapToCsv(Collection<String> collection) {
    return String.join(COLLECTION_ELEMENT_CSV_DELIMITER, collection);
  }

  private Collection<String> csvToCollection(String csvRepr) {
    return Arrays.stream(csvRepr.split(COLLECTION_ELEMENT_CSV_DELIMITER)).filter(Strings::isNotEmpty).collect(Collectors.toList());
  }

}
