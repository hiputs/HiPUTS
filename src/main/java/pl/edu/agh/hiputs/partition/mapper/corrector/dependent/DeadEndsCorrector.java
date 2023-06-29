package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.DeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@RequiredArgsConstructor
public class DeadEndsCorrector implements Corrector{
  private final List<DeadEnd> deadEnds;
  private final DeadEndsFixer fixer;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    return fixer.fixFoundDeadEnds(deadEnds, graph);
  }
}
