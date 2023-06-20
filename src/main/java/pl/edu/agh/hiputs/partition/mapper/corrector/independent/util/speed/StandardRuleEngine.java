package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider.Decider;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@RequiredArgsConstructor
public class StandardRuleEngine implements RuleEngine{
  private final List<Decider> deciders;

  @Override
  public Integer processWay(Edge<JunctionData, WayData> edge) {
    SpeedResultHandler handler = new SpeedResultHandler();
    handler.setEdge(edge);

    deciders.forEach(decider -> decider.decideAboutValue(handler));

    return handler.getResultSpeed();
  }
}
