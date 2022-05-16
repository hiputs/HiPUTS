package pl.edu.agh.hiputs.partition.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.Osm2InternalModelMapper;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.partition.osm.OsmGraphReader;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphReader;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapStructureLoaderImpl implements MapStructureLoader {

  private final OsmGraphReader osmGraphReader;

  private final Osm2InternalModelMapper osm2InternalModelMapper;

  private final PatchPartitioner patchPartitioner;

  private final PatchesGraphReader patchesGraphReader;

  private final PatchesGraphWriter patchesGraphWriter;

  @Override
  public Graph<PatchData, PatchConnectionData> loadFromOsmFile(Path osmFilePath) throws IOException {
    InputStream is = Files.newInputStream(osmFilePath);
    OsmGraph osmGraph = osmGraphReader.loadOsmData(is);
    Graph<JunctionData, WayData> graph = osm2InternalModelMapper.mapToInternalModel(osmGraph);
    Graph<PatchData, PatchConnectionData> patchesGraph = patchPartitioner.partition(graph);
    Path deploymentPath = osmFilePath.getParent();
    try {
      patchesGraphWriter.saveGraphWithPatches(patchesGraph, deploymentPath);
    } catch (IOException e) {
      log.error("Error occurred while saving graph with patches: " + e.getMessage());
    }
    return patchesGraph;
  }

  @Override
  public Graph<PatchData, PatchConnectionData> loadFromCsvImportPackage(Path importPackagePath) {
    return patchesGraphReader.readGraphWithPatches(importPackagePath);
  }
}
