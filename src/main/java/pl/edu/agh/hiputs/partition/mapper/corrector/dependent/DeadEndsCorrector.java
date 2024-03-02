package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.DeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.StandardCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.mapper.transformer.GraphCrossroadDeterminer;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@RequiredArgsConstructor
public class DeadEndsCorrector implements Corrector {

  private final GraphCrossroadDeterminer graphCrossroadDeterminer =
      new GraphCrossroadDeterminer(new StandardCrossroadDeterminer());
  private final List<DeadEnd> deadEnds;
  private final DeadEndsFixer fixer;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    return graphCrossroadDeterminer.transform(fixer.fixFoundDeadEnds(deadEnds, graph));
  }
}
