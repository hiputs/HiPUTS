package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(1)
@RequiredArgsConstructor
public class WayTypeFilter implements Filter {

  private final static String ROAD_KEY = "highway";
  private final ModelConfigurationService modelConfigService;

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {
    Set<String> roadLabels =
        Arrays.stream(modelConfigService.getModelConfig().getWayTypes()).collect(Collectors.toSet());

    return new OsmGraph(osmGraph.getNodes(),
        osmGraph.getWays().stream().filter(osmWay -> isAcceptable(roadLabels, osmWay)).toList(),
        osmGraph.getRelations());
  }

  private boolean isAcceptable(Set<String> roadLabels, OsmWay osmWay) {
    // empty set = no restrictions
    if (roadLabels.isEmpty()) {
      return true;
    }

    Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmWay);

    if (tags.containsKey(ROAD_KEY)) {
      return roadLabels.contains(tags.get(ROAD_KEY));
    } else {
      return false;
    }
  }
}
