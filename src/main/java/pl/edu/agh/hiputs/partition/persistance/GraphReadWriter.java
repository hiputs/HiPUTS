package pl.edu.agh.hiputs.partition.persistance;

import java.io.IOException;
import java.nio.file.Path;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface GraphReadWriter {

  void saveGraphWithPatches(Graph<PatchData, PatchConnectionData> graph, Path exportPath) throws IOException;

  Graph<PatchData, PatchConnectionData> readGraphWithPatches(Path importPath) throws IOException;

}
