package pl.edu.agh.hiputs.server.partition;

import pl.edu.agh.hiputs.server.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;
import pl.edu.agh.hiputs.server.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.server.partition.osm.OsmGraphReader;
import pl.edu.agh.hiputs.server.partition.service.PatchPartitioner;
import pl.edu.agh.hiputs.server.partition.service.TrivialPatchPartitioner;
import pl.edu.agh.hiputs.server.partition.visualisation.graphstream.OsmPartitionerVisualizer;

public class Main {

  public static void main(String[] args) {
    OsmGraphReader osmGraphReader = new OsmGraphReader();
    OsmGraph osmGraph = osmGraphReader.loadOsmData(Main.class.getResourceAsStream("/map.osm"));
    Osm2InternalModelMapper mapper = new Osm2InternalModelMapper();
    Graph<JunctionData, WayData> graph = mapper.osmToInternal(osmGraph);
    PatchPartitioner patchPartitioner = new TrivialPatchPartitioner();
    patchPartitioner.partition(graph);
    OsmPartitionerVisualizer osmPartitionerVisualizer = new OsmPartitionerVisualizer(graph);
    osmPartitionerVisualizer.showGui();
  }
}
