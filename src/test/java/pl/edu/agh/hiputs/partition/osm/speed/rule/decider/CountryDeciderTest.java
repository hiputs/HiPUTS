package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.countryKeyInTags;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.resolver.DefaultCountryResolver;

@ExtendWith(MockitoExtension.class)
public class CountryDeciderTest {
  @Mock
  private DefaultCountryResolver defaultCountryResolver;
  @InjectMocks
  private CountryDecider countryDecider;

  private SpeedResultHandler speedResultHandler;
  private Map<Long, OsmNode> mapOfNodes;
  private OsmWay way;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
  }

  @Test
  public void setDefaultWhenEmptyNodesMap() {
    // given
    mapOfNodes = Collections.emptyMap();
    speedResultHandler.setMapOfOsmNodes(mapOfNodes);

    TLongList listNodeIdsForWay = new TLongArrayList();
    listNodeIdsForWay.add(2L);
    way = new Way(1L, listNodeIdsForWay);
    speedResultHandler.setOsmWay(way);

    // when
    Mockito.when(defaultCountryResolver.getValue()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("UK", speedResultHandler.getCountry());
  }

  @Test
  public void setDefaultWhenEmptyIdsInWay() {
    // given
    mapOfNodes = Map.of(2L, new Node(2L, 1.0, 2.0));
    speedResultHandler.setMapOfOsmNodes(mapOfNodes);

    TLongList listNodeIdsForWay = new TLongArrayList();
    way = new Way(1L, listNodeIdsForWay);
    speedResultHandler.setOsmWay(way);

    // when
    Mockito.when(defaultCountryResolver.getValue()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("UK", speedResultHandler.getCountry());
  }

  @Test
  public void setDefaultWhenNodeNotHaveCountryTag() {
    // given
    mapOfNodes = Map.of(2L, new Node(2L, 1.0, 2.0, Collections.emptyList()));
    speedResultHandler.setMapOfOsmNodes(mapOfNodes);

    TLongList listNodeIdsForWay = new TLongArrayList();
    listNodeIdsForWay.add(2L);
    way = new Way(1L, listNodeIdsForWay);
    speedResultHandler.setOsmWay(way);

    // when
    Mockito.when(defaultCountryResolver.getValue()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("UK", speedResultHandler.getCountry());
  }

  @Test
  public void setFoundValueTag() {
    // given
    mapOfNodes = Map.of(2L, new Node(2L, 1.0, 2.0, List.of(new Tag(countryKeyInTags, "PL"))));
    speedResultHandler.setMapOfOsmNodes(mapOfNodes);

    TLongList listNodeIdsForWay = new TLongArrayList();
    listNodeIdsForWay.add(2L);
    way = new Way(1L, listNodeIdsForWay);
    speedResultHandler.setOsmWay(way);

    // when
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("PL", speedResultHandler.getCountry());
  }
}
