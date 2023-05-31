package pl.edu.agh.hiputs.partition.mapper.filter;

import pl.edu.agh.hiputs.partition.osm.OsmGraph;

public interface Filter {

  OsmGraph filter(OsmGraph osmGraph);
}
