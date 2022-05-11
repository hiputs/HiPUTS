package pl.edu.agh.hiputs.partition;

import java.io.IOException;
import java.nio.file.Path;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapperImpl;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.persistance.GraphReadWriter;
import pl.edu.agh.hiputs.partition.persistance.GraphReadWriterImpl;
import pl.edu.agh.hiputs.partition.service.PatchPartitioner;
import pl.edu.agh.hiputs.partition.service.TrivialPatchPartitioner;
import pl.edu.agh.hiputs.partition.visualisation.graphstream.OsmPartitionerVisualizer;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReader;

public class Main {

  public static void main(String[] args) throws IOException {
    OsmGraphReader osmGraphReader = new OsmGraphReader();
    OsmGraph osmGraph = osmGraphReader.loadOsmData(Main.class.getResourceAsStream("/map.osm"));
    Osm2InternalModelMapperImpl mapper = new Osm2InternalModelMapperImpl();
    Graph<JunctionData, WayData> graph = mapper.mapToInternalModel(osmGraph);
    PatchPartitioner patchPartitioner = new TrivialPatchPartitioner();
    Graph<PatchData, PatchConnectionData> patchesGraph = patchPartitioner.partition(graph);
    Path deploymentPath = Path.of("./");
    GraphReadWriter graphReadWriter = new GraphReadWriterImpl();
    graphReadWriter.saveGraphWithPatches(patchesGraph, deploymentPath);
    graphReadWriter.readGraphWithPatches(deploymentPath);
    OsmPartitionerVisualizer osmPartitionerVisualizer = new OsmPartitionerVisualizer(graph);
    osmPartitionerVisualizer.showGui();
  }
}
