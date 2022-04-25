package pl.edu.agh.partition;

import pl.edu.agh.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.partition.model.Graph;
import pl.edu.agh.partition.osm.OsmGraph;
import pl.edu.agh.partition.osm.OsmGraphReader;
import pl.edu.agh.partition.service.PatchPartitioner;
import pl.edu.agh.partition.service.TrivialPatchPartitioner;
import pl.edu.agh.visualization.graphstream.OsmPartitionerVisualizer;

public class Main {

    public static void main(String[] args) {
        OsmGraphReader osmGraphReader = new OsmGraphReader();
        OsmGraph osmGraph = osmGraphReader.loadOsmData(Main.class.getResourceAsStream("/map.osm"));
        Osm2InternalModelMapper mapper = new Osm2InternalModelMapper();
        Graph graph = mapper.osmToInternal(osmGraph);
        PatchPartitioner patchPartitioner = new TrivialPatchPartitioner();
        patchPartitioner.partition(graph);
        OsmPartitionerVisualizer osmPartitionerVisualizer = new OsmPartitionerVisualizer(graph);
        osmPartitionerVisualizer.showGui();
    }
}
