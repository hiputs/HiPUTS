package pl.edu.agh.hiputs.partition.mapper.queue;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.Detector;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class DetectorQueue implements ServiceQueue<Graph<JunctionData, WayData>, Void>{
  private final List<Detector> detectors;

  @Override
  public Void executeAll(Graph<JunctionData, WayData> graph) {
    detectors.forEach(detector -> detector.detect(graph));

    return null;
  }
}
