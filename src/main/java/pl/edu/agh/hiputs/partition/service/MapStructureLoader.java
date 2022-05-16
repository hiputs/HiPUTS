package pl.edu.agh.hiputs.partition.service;

import java.io.IOException;
import java.nio.file.Path;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface MapStructureLoader {

  Graph<PatchData, PatchConnectionData> loadFromOsmFile(Path osmFilepath) throws IOException;

  Graph<PatchData, PatchConnectionData> loadFromCsvImportPackage(Path importPackagePath);

}
