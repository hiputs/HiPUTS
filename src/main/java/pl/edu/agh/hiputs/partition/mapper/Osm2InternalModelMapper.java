package pl.edu.agh.hiputs.partition.mapper;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

public interface Osm2InternalModelMapper {

  Graph<JunctionData, WayData> mapToInternalModel(OsmGraph osmGraph);

}
