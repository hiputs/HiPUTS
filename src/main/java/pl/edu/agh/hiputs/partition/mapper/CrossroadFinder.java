package pl.edu.agh.hiputs.partition.mapper;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.List;
import java.util.Set;

public interface CrossroadFinder {

  Set<Long> findAll(List<OsmWay> osmWays);

}
