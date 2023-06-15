package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@Service
@Order(5)
public class CompletenessFilter implements Filter{
  private final static String ROAD_KEY = "highway";

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {

    // 1. ways with minimum two nodes
    // 3. ways with tags containing highway
    // 2. nodes with at least one occurrence in ways

    List<OsmWay> processingWays = osmGraph.getWays().stream()
        .filter(osmWay -> OsmModelUtil.nodesAsList(osmWay).size() >= 2)
        .filter(osmWay -> OsmModelUtil.getTagsAsMap(osmWay).containsKey(ROAD_KEY))
        .collect(Collectors.toList());

    Set<Long> nodesOccurrences = processingWays.stream()
        .flatMap(osmWay -> Arrays.stream(OsmModelUtil.nodesAsList(osmWay).toArray()).boxed())
        .collect(Collectors.toSet());

    List<OsmNode> processingNodes = osmGraph.getNodes().stream()
        .filter(osmNode -> nodesOccurrences.contains(osmNode.getId()))
        .collect(Collectors.toList());

    return new OsmGraph(processingNodes, processingWays);
  }
}
