package pl.edu.agh.hiputs.partition.osm.speed.rule.resolver;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import java.util.List;

public interface Resolver {

  void findValue(List<OsmNode> nodes);

  String getValue();
}
