package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
public class ChangeTypesFixer implements TypesFixer{
  private final static String highwayKey = "highway";

  @Override
  public Graph<JunctionData, WayData> fixFoundIncompatibilities(
      List<TypeIncompatibility> typeIncompatibilities, Graph<JunctionData, WayData> graph
  ) {
    typeIncompatibilities.stream()
        .filter(typeIncompatibility -> graph.getEdges().containsKey(typeIncompatibility.getImpactedEdge().getId()))
        .distinct()
        .forEach(typeIncompatibility -> typeIncompatibility.getImpactedEdge()
            .getData().getTags().put(highwayKey, typeIncompatibility.getRequiredType()));

    return graph;
  }
}
