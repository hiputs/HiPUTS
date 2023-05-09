package pl.edu.agh.hiputs.partition.persistance;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

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
      return readGraphWithPatches(
          ExportDescriptor.builder().exportDirAbsolutePath(importPath.toAbsolutePath().toString()).build());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph, ExportDescriptor exportDescriptor)
      throws IOException {
    FileWriter nodesWriter = new FileWriter(exportDescriptor.getNodesFilePath());
    FileWriter edgesWriter = new FileWriter(exportDescriptor.getEdgesFilePath());
    FileWriter patchesWriter = new FileWriter(exportDescriptor.getPatchesFilePath());

    try (CSVPrinter nodesPrinter = new CSVPrinter(nodesWriter,
        CSVFormat.DEFAULT.builder().setHeader(NodeHeaders.class).build());
        CSVPrinter edgesPrinter = new CSVPrinter(edgesWriter,
            CSVFormat.DEFAULT.builder().setHeader(EdgeHeader.class).build());
        CSVPrinter patchesPrinter = new CSVPrinter(patchesWriter,
            CSVFormat.DEFAULT.builder().setHeader(PatchHeader.class).build())) {
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
              mapToCsv(e.getData().getTags()));
        }
      }
    }
  }

  private Graph<PatchData, PatchConnectionData> readGraphWithPatches(ExportDescriptor exportDescriptor)
      throws IOException {
    FileReader nodesReader = new FileReader(exportDescriptor.getNodesFilePath());
    FileReader edgesReader = new FileReader(exportDescriptor.getEdgesFilePath());
    FileReader patchesReader = new FileReader(exportDescriptor.getPatchesFilePath());

    Graph.GraphBuilder<JunctionData, WayData> wholeMapGraph = new GraphBuilder<>();
    Map<String, Graph.GraphBuilder<JunctionData, WayData>> patchId2graphInsidePatch = new HashMap<>();
    Map<String, Node<JunctionData, WayData>> nodeId2Node = new HashMap<>();

    Iterable<CSVRecord> records =
        CSVFormat.DEFAULT.builder().setHeader(NodeHeaders.class).setSkipHeaderRecord(true).build().parse(nodesReader);

    for (CSVRecord record : records) {
      JunctionData junctionData = JunctionData.builder()
          .lon(Double.parseDouble(record.get(NodeHeaders.longitude)))
          .lat(Double.parseDouble(record.get(NodeHeaders.latitude)))
          .isCrossroad(Boolean.parseBoolean(record.get(NodeHeaders.is_crossroad)))
          .patchId(record.get(NodeHeaders.patch_id))
          .tags(csvToMap(record.isSet(NodeHeaders.tags.name()) ? record.get(NodeHeaders.tags) : ""))
          .build();

      if (!patchId2graphInsidePatch.containsKey(record.get(NodeHeaders.patch_id))) {
        patchId2graphInsidePatch.put(record.get(NodeHeaders.patch_id), new GraphBuilder<>());
      }
      Node<JunctionData, WayData> newNode = new Node<>(record.get(NodeHeaders.id), junctionData);
      nodeId2Node.put(record.get(NodeHeaders.id), newNode);
      patchId2graphInsidePatch.get(record.get(NodeHeaders.patch_id)).addNode(newNode);
      wholeMapGraph.addNode(newNode);
    }

    records =
        CSVFormat.DEFAULT.builder().setHeader(EdgeHeader.class).setSkipHeaderRecord(true).build().parse(edgesReader);
    for (CSVRecord record : records) {
      WayData wayData = WayData.builder()
          .length(Double.parseDouble(record.get(EdgeHeader.length)))
          .maxSpeed(Integer.parseInt(record.get(EdgeHeader.max_speed)))
          .isPriorityRoad(Boolean.parseBoolean(record.get(EdgeHeader.is_priority_road)))
          .isOneWay(Boolean.parseBoolean(record.get(EdgeHeader.is_one_way)))
          .patchId(record.get(EdgeHeader.patch_id))
          .tags(csvToMap(record.isSet(EdgeHeader.tags.name()) ? record.get(EdgeHeader.tags) : ""))
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(record.get(EdgeHeader.source) + "->" + record.get(EdgeHeader.target), wayData);
      edge.setSource(nodeId2Node.get(record.get(EdgeHeader.source)));
      edge.setTarget(nodeId2Node.get(record.get(EdgeHeader.target)));
      if (!patchId2graphInsidePatch.containsKey(record.get(EdgeHeader.patch_id))) {
        patchId2graphInsidePatch.put(record.get(EdgeHeader.patch_id), new GraphBuilder<>());
      }
      patchId2graphInsidePatch.get(record.get(EdgeHeader.patch_id)).addEdge(edge);
      wholeMapGraph.addEdge(edge);
    }

    Graph.GraphBuilder<PatchData, PatchConnectionData> resultGraph = new GraphBuilder<>();
    for (String patchId : patchId2graphInsidePatch.keySet()) {
      PatchData patchData = PatchData.builder().graphInsidePatch(patchId2graphInsidePatch.get(patchId).build()).build();
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
