package pl.edu.agh.hiputs.partition.osm.speed.rule.engine;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.maxSpeedKeyInTags;

import com.slimjars.dist.gnu.trove.list.TLongList;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.CountryDecider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.Decider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.ResultDecider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.TypeOfRoadDecider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.transform.SpeedTagTransformer;

@ExtendWith(MockitoExtension.class)
public class RuleEngineImplTest {
  private TypeOfRoadDecider typeOfRoadDecider = Mockito.mock(TypeOfRoadDecider.class);
  @Spy
  private ArrayList<Decider> deciders;
  @Mock
  private SpeedTagTransformer speedTagTransformer;
  @InjectMocks
  private RuleEngineImpl ruleEngine;

  private List<OsmNode> nodesOnWays;
  private List<OsmWay> ways;

  @BeforeEach
  public void init() {
    deciders.addAll(List.of(
        typeOfRoadDecider,
        Mockito.mock(CountryDecider.class),
        Mockito.mock(ResultDecider.class)
    ));

    OsmNode osmNode1 = Mockito.mock(Node.class);
    OsmNode osmNode2 = Mockito.mock(Node.class);
    Mockito.when(osmNode1.getId()).thenReturn(1L);
    Mockito.when(osmNode2.getId()).thenReturn(2L);
    nodesOnWays = List.of(osmNode1, osmNode2);

    ways = new ArrayList<>();
  }

  @Test
  public void notChangingWaysWithMaxSpeedTagAlreadySet() {
    // given
    OsmWay way1 = new Way(1, Mockito.mock(TLongList.class), List.of(new Tag(maxSpeedKeyInTags, "50")));
    OsmWay way2 = new Way(2, Mockito.mock(TLongList.class), List.of(new Tag(maxSpeedKeyInTags, "70")));
    ways.add(way1);
    ways.add(way2);

    // when
    ruleEngine.validateSpeedLimits(nodesOnWays, ways);

    // then
    Assertions.assertEquals(2, ways.size());
    Assertions.assertEquals(way1, ways.get(0));
    Assertions.assertEquals(way2, ways.get(1));
  }

  @Test
  public void removingWaysWithNotClassifiedType() {
    // given
    OsmWay way1 = new Way(1, Mockito.mock(TLongList.class), List.of(new Tag("highway", "path")));
    OsmWay way2 = new Way(2, Mockito.mock(TLongList.class), List.of(new Tag("highway", "proposed")));
    ways.add(way1);
    ways.add(way2);

    // when
    Mockito.doCallRealMethod().when(typeOfRoadDecider).decideAboutValue(Mockito.any(SpeedResultHandler.class));
    ruleEngine.validateSpeedLimits(nodesOnWays, ways);

    // then
    Assertions.assertEquals(0, ways.size());
  }

  @Test
  public void replacingAllWaysWithClassifiedTypes() {
    // given
    OsmWay way1 = new Way(1, Mockito.mock(TLongList.class), List.of(new Tag("highway", "primary")));
    OsmWay way2 = new Way(2, Mockito.mock(TLongList.class), List.of(new Tag("highway", "living_street")));
    ways.add(way1);
    ways.add(way2);

    // when
    Mockito.doCallRealMethod().when(typeOfRoadDecider).decideAboutValue(Mockito.any(SpeedResultHandler.class));
    Mockito.doCallRealMethod().when(speedTagTransformer).replaceValue(Mockito.any(Way.class), Mockito.any());
    ruleEngine.validateSpeedLimits(nodesOnWays, ways);

    // then
    Assertions.assertEquals(2, ways.size());
    Assertions.assertEquals(way1.getId(), ways.get(0).getId());
    Assertions.assertEquals(way2.getId(), ways.get(1).getId());
  }
}
