package pl.edu.agh.hiputs.partition.mapper.filter;

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

public class NodeTypeFilterTest {
  private final NodeTypeFilter filter = new NodeTypeFilter();

  @Test
  public void filterWhenZeroNodesProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of(), List.of());

    // when
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }

  @Test
  public void filterSomeWhenSomeNodesWithoutRoadProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1)
    ), List.of(), List.of());

    // when
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }

  @Test
  public void filterZeroWhenSomeNodesWithRoadProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1),
        new Node(2L, 2, 2)
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}))
    ), List.of());

    // when
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, resultOsmGraph.getNodes().size());
  }
}
