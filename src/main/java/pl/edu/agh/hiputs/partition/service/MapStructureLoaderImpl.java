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

@Slf4j
@Service
@RequiredArgsConstructor
public class MapStructureLoaderImpl implements MapStructureLoader {

  private final OsmGraphReader osmGraphReader;

  private final Osm2InternalModelMapper osm2InternalModelMapper;

  private final PatchPartitioner patchPartitioner;

  private final PatchesGraphReader patchesGraphReader;

  @Override
  public Graph<PatchData, PatchConnectionData> loadFromOsmFile(Path osmFilePath){
    InputStream is;
    try {
      is = Files.newInputStream(osmFilePath);
    } catch (IOException e) {
      log.error(e.getMessage());
      return null;
    }
    OsmGraph osmGraph = osmGraphReader.loadOsmData(is);
    Graph<JunctionData, WayData> graph = osm2InternalModelMapper.mapToInternalModel(osmGraph);
    return patchPartitioner.partition(graph);
  }

  @Override
  public Graph<PatchData, PatchConnectionData> loadFromCsvImportPackage(Path importPackagePath) {
    return patchesGraphReader.readGraphWithPatches(importPackagePath);
  }

}
