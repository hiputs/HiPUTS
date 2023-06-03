package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@Service
@Order(1)
public class WayTypeFilter implements Filter{
  private final static String ROAD_KEY = "highway";
  private final static Set<String> ROAD_LABELS = Set.of(
      "motorway", "trunk", "motorway_link", "trunk_link", "primary", "secondary", "tertiary",
      "unclassified", "primary_link", "secondary_link", "tertiary_link", "living_street",
      "service", "bus_guideway", "busway", "escape", "raceway", "road", "residential"
  );

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {
    osmGraph.getWays().removeAll(
        osmGraph.getWays().stream()
            .filter(osmWay -> !isAcceptable(osmWay))
            .toList()
    );

    return osmGraph;
  }

  private boolean isAcceptable(OsmWay osmWay) {
    Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmWay);

    if (tags.containsKey(ROAD_KEY)) {
      return ROAD_LABELS.contains(tags.get(ROAD_KEY));
    } else {
      return false;
    }
  }
}
