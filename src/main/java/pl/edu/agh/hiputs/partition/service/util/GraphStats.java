package pl.edu.agh.hiputs.partition.service.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Slf4j
@RequiredArgsConstructor
public class GraphStats {

  private final Graph<JunctionData, WayData> roadGraph;
  private final Graph<PatchData, PatchConnectionData> patchesGraph;

  public void logBasicInfo() {
    log.info(String.format("Road graph: nodes number = %d, edges number = %d", roadGraph.getNodes().size(), roadGraph.getEdges().size()));
    log.info(String.format("Patches graph: nodes number = %d, edges number = %d", patchesGraph.getNodes().size(), patchesGraph.getEdges().size()));
  }

  public void saveRoadGraphLaneLengths(String filename) {
    double maxLength = -1.0;
    double minLength = Double.MAX_VALUE;
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
      for (final Double aDouble : roadGraph.getEdges()
          .values()
          .stream()
          .map(e -> e.getData().getLength())
          .toList()) {
        writer.write(aDouble.toString()+'\n');
        if (aDouble < minLength) minLength = aDouble;
        if (aDouble > maxLength) maxLength = aDouble;
      }
      writer.close();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    log.info(String.format("Max lane length for road graph = %f, min length = %f", maxLength, minLength));
  }

}
