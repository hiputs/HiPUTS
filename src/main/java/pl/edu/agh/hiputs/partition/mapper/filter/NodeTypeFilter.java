package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@Service
@Order(2)
public class NodeTypeFilter implements Filter {

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {
    Set<Long> nodesIdsFromWays = osmGraph.getWays()
        .stream()
        .map(OsmModelUtil::nodesAsList)
        .flatMapToLong(list -> Arrays.stream(list.toArray()))
        .boxed()
        .collect(Collectors.toSet());

    return new OsmGraph(
        osmGraph.getNodes().stream().filter(osmNode -> isAcceptable(nodesIdsFromWays, osmNode)).toList(),
        osmGraph.getWays(), osmGraph.getRelations());
  }

  private boolean isAcceptable(Set<Long> nodesIdsFromWays, OsmNode osmNode) {
    // node type is determined by affiliation to road instead of tags
    return nodesIdsFromWays.contains(osmNode.getId());
  }
}
