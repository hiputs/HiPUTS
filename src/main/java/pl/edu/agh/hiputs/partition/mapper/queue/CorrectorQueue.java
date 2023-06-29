package pl.edu.agh.hiputs.partition.mapper.queue;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class CorrectorQueue implements EditableQueue<Corrector>,
    ServiceQueue<Graph<JunctionData, WayData>, Graph<JunctionData, WayData>>{
  private final List<Corrector> registeredCorrectors = new ArrayList<>();
  private final List<Corrector> correctors;

  @Override
  public void registerService(Corrector service) {
    registeredCorrectors.add(service);
  }

  @Override
  public Graph<JunctionData, WayData> executeAll(Graph<JunctionData, WayData> graph) {
    correctors.addAll(0, registeredCorrectors);

    for (final Corrector corrector : correctors) {
      graph = corrector.correct(graph);
    }

    return graph;
  }
}
