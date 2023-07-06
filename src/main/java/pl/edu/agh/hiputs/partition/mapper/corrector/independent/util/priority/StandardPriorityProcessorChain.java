package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component.PriorityProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@RequiredArgsConstructor
public class StandardPriorityProcessorChain implements PriorityProcessorChain {
  private final List<PriorityProcessor> priorityProcessors;

  @PostConstruct
  private void closeChain() {
    priorityProcessors.add(new RandomPriorityProcessor());
  }

  @Override
  public Edge<JunctionData, WayData> getTopPriorityRoad(List<Edge<JunctionData, WayData>> edges) {
    return priorityProcessors.stream()
        .map(priorityProcessor -> priorityProcessor.compareRoads(edges))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .orElse(null);
  }

  private static class RandomPriorityProcessor implements PriorityProcessor{
    private final Random random = new Random();

    @Override
    public Optional<Edge<JunctionData, WayData>> compareRoads(List<Edge<JunctionData, WayData>> edges) {
      if (edges.size() == 0) {
        return Optional.empty();
      }

      return Optional.of(edges.get(random.nextInt(edges.size())));
    }
  }
}
