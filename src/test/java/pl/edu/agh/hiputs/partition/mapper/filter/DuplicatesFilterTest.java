package pl.edu.agh.hiputs.partition.mapper.filter;

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;

@ExtendWith(MockitoExtension.class)
public class DuplicatesFilterTest {

  private final DuplicatesFilter filter = new DuplicatesFilter();

  @Test
  public void mergeNodesByLocationWithUnCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(
        List.of(new Node(1L, 1, 1, List.of(osmTag1), metadata1), new Node(2L, 1, 1, List.of(osmTag2), metadata2),
            new Node(3L, 2, 2, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class)),
            new Way(20L, new TLongArrayList(new long[] {2L, 3L}), List.of(), Mockito.mock(Metadata.class))), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(1L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(2L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, returned.getNodes().size());
    returned.getNodes().stream().filter(osmNode -> osmNode.getId() == 1L).findAny().ifPresent(osmNode -> {
      Assertions.assertEquals(2, osmNode.getNumberOfTags());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag1.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag1.getKey()), osmTag1.getValue());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag2.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag2.getKey()), osmTag2.getValue());
    });
  }

  @Test
  public void mergeNodesByLocationWithCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    OsmTag osmTag3 = new Tag("highway", "secondary");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(osmTag1), metadata1),
        new Node(2L, 1, 1, List.of(osmTag2, osmTag3), metadata2),
        new Node(3L, 2, 2, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class)),
            new Way(20L, new TLongArrayList(new long[] {2L, 3L}), List.of(), Mockito.mock(Metadata.class))), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(2L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(1L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, returned.getNodes().size());
    returned.getNodes().stream().filter(osmNode -> osmNode.getId() == 1L).findAny().ifPresent(osmNode -> {
      Assertions.assertEquals(2, osmNode.getNumberOfTags());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag1.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag1.getKey()), osmTag3.getValue());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag2.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag2.getKey()), osmTag2.getValue());
    });
  }

  @Test
  public void mergeWaysByLocationWithUnCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(), Mockito.mock(Metadata.class)),
        new Node(2L, 2, 2, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 2L}), List.of(osmTag1), metadata1),
            new Way(20L, new TLongArrayList(new long[] {1L, 2L}), List.of(osmTag2), metadata2)), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(1L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(2L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, returned.getWays().size());
    Assertions.assertEquals(2, returned.getWays().get(0).getNumberOfTags());
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).containsKey(osmTag1.getKey()));
    Assertions.assertEquals(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).get(osmTag1.getKey()),
        osmTag1.getValue());
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).containsKey(osmTag2.getKey()));
    Assertions.assertEquals(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).get(osmTag2.getKey()),
        osmTag2.getValue());
  }

  @Test
  public void mergeWaysByLocationWithCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    OsmTag osmTag3 = new Tag("highway", "secondary");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(), Mockito.mock(Metadata.class)),
        new Node(2L, 2, 2, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 2L}), List.of(osmTag1), metadata1),
            new Way(20L, new TLongArrayList(new long[] {1L, 2L}), List.of(osmTag2, osmTag3), metadata2)), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(2L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(1L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(1, returned.getWays().size());
    Assertions.assertEquals(2, returned.getWays().get(0).getNumberOfTags());
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).containsKey(osmTag1.getKey()));
    Assertions.assertEquals(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).get(osmTag1.getKey()),
        osmTag3.getValue());
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).containsKey(osmTag2.getKey()));
    Assertions.assertEquals(OsmModelUtil.getTagsAsMap(returned.getWays().get(0)).get(osmTag2.getKey()),
        osmTag2.getValue());
  }

  @Test
  public void mergeNodesByIdWithUnCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(
        List.of(new Node(1L, 1, 1, List.of(osmTag1), metadata1), new Node(1L, 2, 2, List.of(osmTag2), metadata2),
            new Node(3L, 3, 3, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class)),
            new Way(20L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class))), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(1L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(2L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, returned.getNodes().size());
    returned.getNodes().stream().filter(osmNode -> osmNode.getId() == 1L).findAny().ifPresent(osmNode -> {
      Assertions.assertEquals(2, osmNode.getNumberOfTags());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag1.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag1.getKey()), osmTag1.getValue());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag2.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag2.getKey()), osmTag2.getValue());
    });
  }

  @Test
  public void mergeNodesByIdWithCommonTags() {
    // given
    OsmTag osmTag1 = new Tag("highway", "primary");
    OsmTag osmTag2 = new Tag("name", "Santa road");
    OsmTag osmTag3 = new Tag("highway", "secondary");
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(osmTag1), metadata1),
        new Node(1L, 2, 2, List.of(osmTag2, osmTag3), metadata2),
        new Node(3L, 3, 3, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class)),
            new Way(20L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class))), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(2L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(1L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, returned.getNodes().size());
    returned.getNodes().stream().filter(osmNode -> osmNode.getId() == 1L).findAny().ifPresent(osmNode -> {
      Assertions.assertEquals(2, osmNode.getNumberOfTags());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag1.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag1.getKey()), osmTag3.getValue());
      Assertions.assertTrue(OsmModelUtil.getTagsAsMap(osmNode).containsKey(osmTag2.getKey()));
      Assertions.assertEquals(OsmModelUtil.getTagsAsMap(osmNode).get(osmTag2.getKey()), osmTag2.getValue());
    });
  }

  @Test
  public void deleteWaysByIdWithoutDeletingNodes() {
    // given
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(), Mockito.mock(Metadata.class)),
        new Node(2L, 2, 2, List.of(), Mockito.mock(Metadata.class)),
        new Node(3L, 3, 3, List.of(), Mockito.mock(Metadata.class)),
        new Node(4L, 4, 4, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 2L}), List.of(), metadata1),
            new Way(10L, new TLongArrayList(new long[] {3L, 4L}), List.of(), metadata2),
            new Way(30L, new TLongArrayList(new long[] {1L, 3L}), List.of(), Mockito.mock(Metadata.class)),
            new Way(40L, new TLongArrayList(new long[] {2L, 4L}), List.of(), Mockito.mock(Metadata.class))), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(1L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(2L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(4, returned.getNodes().size());
    Assertions.assertEquals(3, returned.getWays().size());
    returned.getWays().stream().filter(osmWay -> osmWay.getId() == 10L).findAny().ifPresent(foundWay -> {
      Assertions.assertTrue(OsmModelUtil.nodesAsList(foundWay).contains(1L));
      Assertions.assertTrue(OsmModelUtil.nodesAsList(foundWay).contains(2L));
    });
  }

  @Test
  public void deleteWaysByIdWithDeletingNodes() {
    // given
    Metadata metadata1 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    Metadata metadata2 = Mockito.mock(Metadata.class, Mockito.RETURNS_DEEP_STUBS);
    OsmGraph osmGraph = new OsmGraph(List.of(new Node(1L, 1, 1, List.of(), Mockito.mock(Metadata.class)),
        new Node(2L, 2, 2, List.of(), Mockito.mock(Metadata.class)),
        new Node(3L, 3, 3, List.of(), Mockito.mock(Metadata.class)),
        new Node(4L, 4, 4, List.of(), Mockito.mock(Metadata.class))),
        List.of(new Way(10L, new TLongArrayList(new long[] {1L, 2L}), List.of(), metadata1),
            new Way(10L, new TLongArrayList(new long[] {3L, 4L}), List.of(), metadata2)), List.of());

    // when
    Mockito.when(metadata1.getTimestamp()).thenReturn(1L);
    Mockito.when(metadata2.getTimestamp()).thenReturn(2L);
    OsmGraph returned = filter.filter(osmGraph);

    // then
    Assertions.assertEquals(2, returned.getNodes().size());
    Assertions.assertEquals(1, returned.getWays().size());
    returned.getWays().stream().filter(osmWay -> osmWay.getId() == 10L).findAny().ifPresent(foundWay -> {
      Assertions.assertTrue(OsmModelUtil.nodesAsList(foundWay).contains(1L));
      Assertions.assertTrue(OsmModelUtil.nodesAsList(foundWay).contains(2L));
    });
  }
}
