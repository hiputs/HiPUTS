package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface CountryResolver {

  void deduceCountry(Graph<JunctionData, WayData> graph);

  String getCountry();
}
