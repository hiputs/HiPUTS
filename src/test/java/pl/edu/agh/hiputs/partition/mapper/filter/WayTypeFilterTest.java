package pl.edu.agh.hiputs.partition.mapper.filter;

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class WayTypeFilterTest {
  private final ModelConfigurationService modelConfigService = Mockito.mock(
      ModelConfigurationService.class, Mockito.RETURNS_DEEP_STUBS);
  private final WayTypeFilter filter = new WayTypeFilter(modelConfigService);

  @Test
  public void filterWhenZeroWaysProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of());

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayTypes()).thenReturn(new String[]{"primary"});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getWays().isEmpty());
  }

  @Test
  public void filterZeroWhenSomeWaysProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(),
        List.of(new Way(1L, new TLongArrayList(), List.of(new Tag("highway", "primary")))));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayTypes()).thenReturn(new String[]{"primary"});    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
  }

  @Test
  public void filterSomeWhenSomeWaysProvided() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of(
        new Way(1L, new TLongArrayList(), List.of(new Tag("highway", "primary"))),
        new Way(1L, new TLongArrayList(), List.of(new Tag("highway", "pedestrian")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayTypes()).thenReturn(new String[]{"primary"});    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
  }

  @Test
  public void filterZeroWhenSomeWaysProvidedButConfigAllowsAll() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of(
        new Way(1L, new TLongArrayList(), List.of(new Tag("highway", "primary"))),
        new Way(1L, new TLongArrayList(), List.of(new Tag("highway", "pedestrian")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayTypes()).thenReturn(new String[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, resultOsmGraph.getWays().size());
  }
}
