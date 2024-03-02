package pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface IncompatibilityFinder {

  List<TypeIncompatibility> lookup(Graph<JunctionData, WayData> graph);

}
