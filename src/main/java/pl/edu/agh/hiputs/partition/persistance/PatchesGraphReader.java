package pl.edu.agh.hiputs.partition.persistance;

import java.nio.file.Path;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface PatchesGraphReader {

  Graph<PatchData, PatchConnectionData> readGraphWithPatches(Path importPath);

}
