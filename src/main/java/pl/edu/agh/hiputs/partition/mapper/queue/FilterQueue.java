package pl.edu.agh.hiputs.partition.mapper.queue;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.filter.Filter;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@Service
@RequiredArgsConstructor
public class FilterQueue implements ServiceQueue<OsmGraph, OsmGraph>{
  private final List<Filter> filters;

  @Override
  public OsmGraph executeAll(OsmGraph graph) {
    for (final Filter filter : filters) {
      graph = filter.filter(graph);
    }

    return graph;
  }
}
