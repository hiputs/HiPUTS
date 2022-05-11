package pl.edu.agh.hiputs.partition;

import java.io.IOException;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.persistance.GraphReadWriter;
import pl.edu.agh.hiputs.partition.service.PatchPartitioner;
import pl.edu.agh.hiputs.partition.service.TrivialPatchPartitioner;
import pl.edu.agh.hiputs.partition.visualisation.graphstream.OsmPartitionerVisualizer;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReader;

public class Main {

  public static void main(String[] args) throws IOException {
    OsmGraphReader osmGraphReader = new OsmGraphReader();
    OsmGraph osmGraph = osmGraphReader.loadOsmData(Main.class.getResourceAsStream("/map.osm"));
    Osm2InternalModelMapper mapper = new Osm2InternalModelMapper();
    Graph<JunctionData, WayData> graph = mapper.osmToInternal(osmGraph);
    PatchPartitioner patchPartitioner = new TrivialPatchPartitioner();
    Graph<PatchData, PatchConnectionData> patchesGraph = patchPartitioner.partition(graph);
    (new GraphReadWriter()).saveGraphWithPatches(patchesGraph);
    (new GraphReadWriter()).readGraphWithPatches();
    OsmPartitionerVisualizer osmPartitionerVisualizer = new OsmPartitionerVisualizer(graph);
    osmPartitionerVisualizer.showGui();
  }
}
