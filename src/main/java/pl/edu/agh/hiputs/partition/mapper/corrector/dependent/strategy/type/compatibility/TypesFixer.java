package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface TypesFixer {

  Graph<JunctionData, WayData> fixFoundIncompatibilities(
      List<TypeIncompatibility> typeIncompatibilities,
      Graph<JunctionData, WayData> graph
  );
}
