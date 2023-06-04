package pl.edu.agh.hiputs.partition.mapper.filter;

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration.TagEntry;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class SelectionFilterTest {
  private final ModelConfigurationService modelConfigService = Mockito.mock(
      ModelConfigurationService.class, Mockito.RETURNS_DEEP_STUBS);
  private final SelectionFilter filter = new SelectionFilter(modelConfigService);

  @Test
  public void filterZeroWhenEmptyMapsAndGraph() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of());

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getWays().isEmpty());
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }

  @Test
  public void filterZeroWhenEmptyMapsOnly() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1)
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L}))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
    Assertions.assertEquals(1, resultOsmGraph.getNodes().size());
  }

  @Test
  public void filterZeroWhenEmptyGraphOnly() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(), List.of());

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50")});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("oneway", "50")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("natural", "tree")});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getWays().isEmpty());
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }


  @Test
  public void filterSomeWhenPermittedSpecificEntries() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1, List.of(new Tag("highway", "traffic_signals"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("maxspeed", "50"))),
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("country", "Poland")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50")});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
    Assertions.assertEquals(1, resultOsmGraph.getNodes().size());
  }

  @Test
  public void filterSomeWhenPermittedSpecificEntriesButWrongValues() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1, List.of(new Tag("highway", "barrier"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("maxspeed", "40"))),
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("country", "Poland")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50")});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getWays().isEmpty());
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }

  @Test
  public void filterSomeWhenPermittedMoreSpecificEntries() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1, List.of(new Tag("highway", "traffic_signals"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("maxspeed", "50"))),
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("country", "Poland")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50"), new TagEntry("highway", "secondary")});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals"), new TagEntry("name", "unknown")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertTrue(resultOsmGraph.getWays().isEmpty());
    Assertions.assertTrue(resultOsmGraph.getNodes().isEmpty());
  }

  @Test
  public void filterSomeWhenProhibitedSpecificEntries() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1, List.of(new Tag("highway", "traffic_signals"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("maxspeed", "50"))),
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("country", "Poland")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals")});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
    Assertions.assertEquals(1, resultOsmGraph.getNodes().size());
  }

  @Test
  public void filterZeroWhenProhibitedSpecificEntriesButWrongValues() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(new Tag("highway", "barrier"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))),
        List.of(new Way(1L, new TLongArrayList(new long[] {1L, 2L}), List.of(new Tag("maxspeed", "40"))),
            new Way(1L, new TLongArrayList(new long[] {1L, 2L}), List.of(new Tag("country", "Poland")))));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[] {});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[] {new TagEntry("maxspeed", "50")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[] {});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[] {new TagEntry("highway", "traffic_signals")});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, resultOsmGraph.getWays().size());
    Assertions.assertEquals(2, resultOsmGraph.getNodes().size());
  }

  @Test
  public void filterSomeWhenProhibitedMoreSpecificEntries() {
    // given
    OsmGraph osmGraph = new OsmGraph(List.of(
        new Node(1L, 1, 1, List.of(new Tag("highway", "traffic_signals"), new Tag("name", "entry"))),
        new Node(2L, 2, 2, List.of(new Tag("name", "entry")))
    ), List.of(
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("maxspeed", "50"), new Tag("name", "road"))),
        new Way(1L, new TLongArrayList(new long[]{1L, 2L}), List.of(new Tag("country", "Poland")))
    ));

    // when
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("maxspeed", "50"), new TagEntry("highway", "secondary")});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
        .thenReturn(new TagEntry[]{});
    Mockito.when(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
        .thenReturn(new TagEntry[]{new TagEntry("highway", "traffic_signals"), new TagEntry("name", "unknown")});
    OsmGraph resultOsmGraph = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, resultOsmGraph.getWays().size());
    Assertions.assertEquals(1, resultOsmGraph.getNodes().size());
  }
}
