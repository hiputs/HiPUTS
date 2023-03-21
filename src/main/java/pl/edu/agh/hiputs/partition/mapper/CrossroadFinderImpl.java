package pl.edu.agh.hiputs.partition.mapper;

import com.slimjars.dist.gnu.trove.list.TLongList;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrossroadFinderImpl implements CrossroadFinder {

  @Override
  public Set<Long> findAll(List<OsmWay> osmWays) {
    return osmWays.stream()
        .map(OsmModelUtil::nodesAsList)
        .map(this::mapTLongListToMap)
        .map(Map::entrySet)
        .flatMap(Set::stream)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, Long::sum))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 2)
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  private Map<Long, Long> mapTLongListToMap(TLongList nodes) {
    Map<Long, Long> counts = Arrays.stream(nodes.toArray())
        .boxed()
        .collect(Collectors.toMap(Function.identity(), node -> 2L, (old, updated) -> 2L));
    counts.put(nodes.get(0), 1L);
    counts.put(nodes.get(nodes.size() - 1), 1L);

    return counts;
  }
}
