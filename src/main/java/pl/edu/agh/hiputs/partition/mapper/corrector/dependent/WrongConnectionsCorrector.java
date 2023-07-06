package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility.TypesFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@RequiredArgsConstructor
public class WrongConnectionsCorrector implements Corrector {
  private final List<TypeIncompatibility> typeIncompatibilities;
  private final TypesFixer fixer;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    fixer.fixFoundIncompatibilities(typeIncompatibilities, graph);

    return graph;
  }
}
