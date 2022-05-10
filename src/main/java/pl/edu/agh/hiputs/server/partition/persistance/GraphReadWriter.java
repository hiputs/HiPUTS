package pl.edu.agh.hiputs.server.partition.persistance;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.server.partition.model.PatchData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Edge;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.server.partition.model.graph.Node;

public class GraphReadWriter {
  private static final String COLLECTION_ELEMENT_CSV_DELIMITER = "#";
  private static final String MAP_KEY_VALUE_PAIR_CSV_DELIMITER = "::";

  public void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph, ExportDescriptor exportDescriptor) throws IOException {
    FileWriter nodesWriter = new FileWriter(exportDescriptor.getNodesFileName());
    FileWriter edgesWriter = new FileWriter(exportDescriptor.getEdgesFileName());
    FileWriter patchesWriter = new FileWriter(exportDescriptor.getPatchesFileName());

    try (CSVPrinter nodesPrinter = new CSVPrinter(nodesWriter, CSVFormat.DEFAULT.builder().setHeader(NodeHeaders.class).build());
        CSVPrinter edgesPrinter = new CSVPrinter(edgesWriter, CSVFormat.DEFAULT.builder().setHeader(EdgeHeader.class).build());
        CSVPrinter patchesPrinter = new CSVPrinter(patchesWriter, CSVFormat.DEFAULT.builder().setHeader(PatchHeader.class).build())) {
      for (Node<PatchData, PatchConnectionData> p : graph.getNodes().values()) {
        patchesPrinter.printRecord(p.getId(),
            mapToCsv(p.getOutgoingEdges().stream().map(e -> e.getTarget().getId()).collect(Collectors.toList())));

        for (Node<JunctionData, WayData> n : p.getData().getGraphInsidePatch().getNodes().values()) {
          nodesPrinter.printRecord(
              n.getId(),
              n.getData().getLat(),
              n.getData().getLon(),
              n.getData().getPatchId(),
              mapToCsv(n.getData().getTags()));
        }

        for (Edge<JunctionData, WayData> e : p.getData().getGraphInsidePatch().getEdges().values()) {
          edgesPrinter.printRecord(
              e.getSource().getId(),
              e.getTarget().getId(),
              e.getData().getLength(),
              e.getData().getMaxSpeed(),
              e.getData().getPatchId(),
              mapToCsv(e.getData().getTags()));
        }
      }
    }
  }

  public void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph) throws IOException {
    saveGraphWithPatches(graph, ExportDescriptor.builder().build());
  }

  public Graph<PatchData, PatchConnectionData> readGraphWithPatches(ExportDescriptor exportDescriptor) throws IOException {
    FileReader nodesReader = new FileReader(exportDescriptor.getNodesFileName());
    FileReader edgesReader = new FileReader(exportDescriptor.getEdgesFileName());
    FileReader patchesReader = new FileReader(exportDescriptor.getPatchesFileName());

    Map<String, Graph.GraphBuilder<JunctionData, WayData>> insideGraphs = new HashMap<>();

    Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
        .setHeader(NodeHeaders.class)
        .setSkipHeaderRecord(true)
        .build()
        .parse(nodesReader);

    for (CSVRecord record : records) {
      JunctionData junctionData = JunctionData.builder()
          .lon(Double.parseDouble(record.get("longitude")))
          .lat(Double.parseDouble(record.get("latitude")))
          .patchId(record.get("patch_id"))
          .tags(csvToMap(record.get("tags")))
          .build();

      if(!insideGraphs.containsKey(record.get("patch_id"))) {
        insideGraphs.put(record.get("patch_id"), new GraphBuilder<>());
      }
      insideGraphs.get(record.get("patch_id")).addNode(new Node<>(record.get("id"), junctionData));
    }

    records = CSVFormat.DEFAULT.builder()
        .setHeader(EdgeHeader.class)
        .setSkipHeaderRecord(true)
        .build().parse(edgesReader);
    for (CSVRecord record : records) {
      WayData wayData = WayData.builder()
          .length(Double.parseDouble(record.get("length")))
          .maxSpeed(Integer.parseInt(record.get("max_speed")))
          .patchId(record.get("patch_id"))
          .tags(csvToMap(record.get("tags")))
          .build();
      Edge<JunctionData, WayData> edge = new Edge<>(record.get("source") + "->" + record.get("target"), wayData);
      edge.setSource(new Node<>(record.get("source"), null));
      edge.setTarget(new Node<>(record.get("target"), null));
      if(!insideGraphs.containsKey(record.get("patch_id"))) {
        insideGraphs.put(record.get("patch_id"), new GraphBuilder<>());
      }
      insideGraphs.get(record.get("patch_id")).addEdge(edge);
    }

    Graph.GraphBuilder<PatchData, PatchConnectionData> resultGraph = new GraphBuilder<>();
    for(String patchId : insideGraphs.keySet()) {
      PatchData patchData = PatchData.builder()
          .graphInsidePatch(insideGraphs.get(patchId).build())
          .build();
      resultGraph.addNode(new Node<>(patchId, patchData));
    }

    records = CSVFormat.DEFAULT.builder()
        .setHeader(PatchHeader.class)
        .setSkipHeaderRecord(true)
        .build().parse(patchesReader);
    for(CSVRecord record : records) {
      record.get("id");
      for(String neighbouringPatchId : csvToCollection(record.get("neighbouring_patches_ids"))) {
        Edge<PatchData, PatchConnectionData> edge = new Edge<>(record.get("id") + "->" + neighbouringPatchId, null);
        edge.setSource(new Node<>(record.get("id"), null));
        edge.setTarget(new Node<>(neighbouringPatchId, null));
        resultGraph.addEdge(edge);
      }
    }

    return resultGraph.build();
  }

  public Graph<PatchData, PatchConnectionData> readGraphWithPatches() throws IOException {
    return readGraphWithPatches(ExportDescriptor.builder().build());
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
        .map(e -> Pair.of(
            e.split(MAP_KEY_VALUE_PAIR_CSV_DELIMITER)[0],
            e.split(MAP_KEY_VALUE_PAIR_CSV_DELIMITER)[1]))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private String mapToCsv(Collection<String> collection) {
    return String.join(COLLECTION_ELEMENT_CSV_DELIMITER, collection);
  }

  private Collection<String> csvToCollection(String csvRepr) {
    return Arrays.stream(csvRepr.split(COLLECTION_ELEMENT_CSV_DELIMITER))
        .collect(Collectors.toList());
  }

}
