package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import com.slimjars.dist.gnu.trove.list.TLongList;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.TypeOfRoad;

@ExtendWith(MockitoExtension.class)
public class TypeOfRoadDeciderTest {
  @InjectMocks
  private TypeOfRoadDecider typeOfRoadDecider;

  private SpeedResultHandler speedResultHandler;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
  }

  @Test
  public void notSettingAndThrow() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class));
    speedResultHandler.setOsmWay(osmWay);

    // when

    // then
    Assertions.assertThrows(IllegalArgumentException.class, () -> typeOfRoadDecider.decideAboutValue(speedResultHandler));
  }

  @Test
  public void settingHighwayTypeByLabel() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class), List.of(new Tag("highway", "motorway")));
    speedResultHandler.setOsmWay(osmWay);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Highway, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingUrbanTypeByNameAndRural() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class), List.of(new Tag("highway", "primary"),
        new Tag("name", "Zwierzyniecka")));
    speedResultHandler.setOsmWay(osmWay);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Urban, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingUrbanTypeByLabel() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class), List.of(new Tag("highway", "living_street")));
    speedResultHandler.setOsmWay(osmWay);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Urban, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingRuralTypeByLabel() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class), List.of(new Tag("highway", "secondary")));
    speedResultHandler.setOsmWay(osmWay);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Rural, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingNotClassifiedTypeByLabel() {
    // given
    OsmWay osmWay = new Way(1L, Mockito.mock(TLongList.class), List.of(new Tag("highway", "path")));
    speedResultHandler.setOsmWay(osmWay);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.NotClassified, speedResultHandler.getTypeOfRoad());
  }
}
