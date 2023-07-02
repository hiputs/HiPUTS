package pl.edu.agh.hiputs.partition.mapper.detector.util.complex;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface ComplexityFinder {

  List<ComplexCrossroad> lookup(Graph<JunctionData, WayData> graph);

}
