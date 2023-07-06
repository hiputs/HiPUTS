package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(1)
@RequiredArgsConstructor
public class TypePriorityProcessor extends AbstractPriorityProcessor{
  private final static String highwayKey = "highway";
  private final ModelConfigurationService modelConfigService;

  @Override
  public Optional<Edge<JunctionData, WayData>> compareRoads(List<Edge<JunctionData, WayData>> edges) {
    return compareRoadsByValue(edges, this::getPriority);
  }

  private int getPriority(Edge<JunctionData, WayData> edge) {
    String type = edge.getData().getTags().get(highwayKey);

    if (type == null) {
      return 0;
    }

    return modelConfigService.getWayTypesPriority().getOrDefault(type, 0);
  }
}
