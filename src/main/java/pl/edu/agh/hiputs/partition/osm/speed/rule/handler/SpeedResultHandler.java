package pl.edu.agh.hiputs.partition.osm.speed.rule.handler;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeedResultHandler {
  private Map<Long, OsmNode> mapOfOsmNodes;
  private OsmWay osmWay;
  private String country, resultSpeed;
  private TypeOfRoad typeOfRoad;
}
