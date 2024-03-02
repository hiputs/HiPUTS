package pl.edu.agh.hiputs.partition.mapper.helper.service.oneway;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StandardOsmAndRounbaboutOnewayProcessorTest {
  private final StandardOsmAndRoundaboutOnewayProcessor processor = new StandardOsmAndRoundaboutOnewayProcessor();

  @Test
  public void checkOnEmpty() {
    // given
    Map<String, String> tags = Collections.emptyMap();

    // when

    // then
    Assertions.assertFalse(processor.checkFromTags(tags));
  }

  @Test
  public void checkTypicalOneway() {
    // given
    Map<String, String> tags = Map.of("oneway", "yes");

    // when

    // then
    Assertions.assertTrue(processor.checkFromTags(tags));
  }

  @Test
  public void checkTypicalBidirectionalWay() {
    // given
    Map<String, String> tags = Map.of("oneway", "no");

    // when

    // then
    Assertions.assertFalse(processor.checkFromTags(tags));
  }

  @Test
  public void checkRoundaboutWay() {
    // given
    Map<String, String> tags = Map.of("junction", "roundabout");

    // when

    // then
    Assertions.assertTrue(processor.checkFromTags(tags));
  }

  @Test
  public void checkOnewayAndRoundabout() {
    // given
    Map<String, String> tags = Map.of("oneway", "yes", "junction", "roundabout");

    // when

    // then
    Assertions.assertTrue(processor.checkFromTags(tags));
  }
}
