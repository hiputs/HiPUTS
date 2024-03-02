package pl.edu.agh.hiputs.partition.mapper.detector.util.end;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface DeadEndsFinder {

  List<DeadEnd> lookup(Graph<JunctionData, WayData> graph);

}
