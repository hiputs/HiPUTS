package pl.edu.agh.hiputs.partition.osm.speed.rule.engine;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.List;

public interface RuleEngine {

  void findDefaultCountry(List<OsmNode> nodes);

  void validateSpeedLimits(List<OsmNode> nodesOnWays, List<OsmWay> ways);

}
