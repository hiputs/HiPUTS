package pl.edu.agh.hiputs.partition.osm.speed.rule.transform;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.maxSpeedKeyInTags;

import com.slimjars.dist.gnu.trove.list.TLongList;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpeedTagTransformerTest {
  @InjectMocks
  private SpeedTagTransformer speedTagTransformer;

  @Test
  public void replaceValueOnEmptyTags() {
    // given
    String newValue = "50";
    OsmWay wayToChange = new Way(
        1L, Mockito.mock(TLongList.class), Collections.emptyList(), Mockito.mock(OsmMetadata.class)
    );

    // when
    OsmWay wayAfter = speedTagTransformer.replaceValue(wayToChange, newValue);

    // then
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(wayAfter).containsKey(maxSpeedKeyInTags));
    Assertions.assertEquals(newValue, OsmModelUtil.getTagsAsMap(wayAfter).get(maxSpeedKeyInTags));
  }

  @Test
  public void replaceValueOnTagsWithoutMaxSpeed() {
    // given
    String newValue = "50";
    OsmWay wayToChange = new Way(
        1L, Mockito.mock(TLongList.class), List.of(new Tag("k1", "v1")), Mockito.mock(OsmMetadata.class)
    );

    // when
    OsmWay wayAfter = speedTagTransformer.replaceValue(wayToChange, newValue);

    // then
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(wayAfter).containsKey(maxSpeedKeyInTags));
    Assertions.assertEquals(newValue, OsmModelUtil.getTagsAsMap(wayAfter).get(maxSpeedKeyInTags));
  }

  @Test
  public void replaceValueOnTagsWithMaxSpeed() {
    // given
    String newValue = "50";
    OsmWay wayToChange = new Way(1L, Mockito.mock(TLongList.class),
        List.of(new Tag("k1", "v1"), new Tag("maxspeed", "60")), Mockito.mock(OsmMetadata.class)
    );

    // when
    OsmWay wayAfter = speedTagTransformer.replaceValue(wayToChange, newValue);

    // then
    Assertions.assertTrue(OsmModelUtil.getTagsAsMap(wayAfter).containsKey(maxSpeedKeyInTags));
    Assertions.assertEquals(newValue, OsmModelUtil.getTagsAsMap(wayAfter).get(maxSpeedKeyInTags));
  }
}
