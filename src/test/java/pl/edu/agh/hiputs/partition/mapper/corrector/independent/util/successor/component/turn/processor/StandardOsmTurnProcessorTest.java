package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.model.WayData;

@ExtendWith(MockitoExtension.class)
public class StandardOsmTurnProcessorTest {
  private final StandardOsmTurnProcessor processor = new StandardOsmTurnProcessor();

  @Test
  public void noDataProvided() {
    // given
    WayData wayData = Mockito.mock(WayData.class);

    // when
    Mockito.when(wayData.getTags()).thenReturn(Collections.emptyMap());

    // then
    Assertions.assertTrue(processor.getTurnDirectionsFromTags(wayData).isEmpty());
  }

  @ParameterizedTest
  @MethodSource("paramsForSingleKeyTest")
  public void singleKey(String key, boolean oppositeMeaning) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of(key, "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.getLanes().size()).thenReturn(1);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(oppositeMeaning);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertFalse(turns.isEmpty());
    Assertions.assertFalse(turns.get(0).isEmpty());
    Assertions.assertEquals(TurnDirection.RIGHT, turns.get(0).get(0));
  }

  @ParameterizedTest
  @MethodSource("paramsForSingleKeyTest")
  public void singleKeyProvidedReturnEmptyList(String key, boolean oppositeMeaning) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of(key, "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(!oppositeMeaning);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertTrue(turns.isEmpty());
  }

  private static Stream<Arguments> paramsForSingleKeyTest() {
    return Stream.of(
        Arguments.of("turn", false),
        Arguments.of("turn:forward", false),
        Arguments.of("turn:backward", true)
    );
  }

  @Test
  public void bothWayKey() {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of("turn:both_ways", "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.getLanes().size()).thenReturn(1);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertFalse(turns.isEmpty());
    Assertions.assertFalse(turns.get(0).isEmpty());
    Assertions.assertEquals(TurnDirection.RIGHT, turns.get(0).get(0));
  }

  @ParameterizedTest
  @MethodSource("paramsForSingleLaneKeyTest")
  public void singleLaneKey(String key, boolean oppositeMeaning) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of(key, "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(oppositeMeaning);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertFalse(turns.isEmpty());
    Assertions.assertFalse(turns.get(0).isEmpty());
    Assertions.assertEquals(TurnDirection.RIGHT, turns.get(0).get(0));
  }

  @ParameterizedTest
  @MethodSource("paramsForSingleLaneKeyTest")
  public void singleLaneKeyProvidedReturnEmptyList(String key, boolean oppositeMeaning) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of(key, "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(!oppositeMeaning);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertTrue(turns.isEmpty());
  }

  private static Stream<Arguments> paramsForSingleLaneKeyTest() {
    return Stream.of(
        Arguments.of("turn:lanes", false),
        Arguments.of("turn:lanes:forward", false),
        Arguments.of("turn:lanes:backward", true)
    );
  }

  @Test
  public void laneBothWayKey() {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of("turn:lanes:both_ways", "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertFalse(turns.isEmpty());
    Assertions.assertFalse(turns.get(0).isEmpty());
    Assertions.assertEquals(TurnDirection.RIGHT, turns.get(0).get(0));
  }

  @ParameterizedTest
  @MethodSource("paramsForForwardAndBackwardKeyTest")
  public void forwardAndBackwardKey(boolean oppositeMeaning, TurnDirection expectedResult, boolean asLanes) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = asLanes ?
        Map.of("turn:lanes:forward", "right", "turn:lanes:backward", "left") :
        Map.of("turn:forward", "right", "turn:backward", "left");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(oppositeMeaning);
    if (!asLanes) {
      Mockito.when(wayData.getLanes().size()).thenReturn(1);
    }

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertFalse(turns.isEmpty());
    Assertions.assertFalse(turns.get(0).isEmpty());
    Assertions.assertEquals(expectedResult, turns.get(0).get(0));
  }

  private static Stream<Arguments> paramsForForwardAndBackwardKeyTest() {
    return Stream.of(
        Arguments.of(false, TurnDirection.RIGHT, false),
        Arguments.of(true, TurnDirection.LEFT, false),
        Arguments.of(false, TurnDirection.RIGHT, true),
        Arguments.of(true, TurnDirection.LEFT, true)
    );
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 4})
  public void multiplyingTurnsPerLaneNo(int lanesNo) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of("turn", "right");

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.getLanes().size()).thenReturn(lanesNo);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(false);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertEquals(Math.max(lanesNo, 1), turns.size());
    IntStream.range(0, lanesNo)
        .forEach(index -> {
          Assertions.assertEquals(1, turns.get(index).size());
          Assertions.assertEquals(List.of(TurnDirection.RIGHT), turns.get(index));
        });
  }

  @ParameterizedTest
  @MethodSource("paramsForDividingTurnsTest")
  public void dividingTurns(String value, List<TurnDirection> expectedTurns) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of("turn", value);

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.getLanes().size()).thenReturn(1);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(false);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertEquals(1, turns.size());
    Assertions.assertEquals(expectedTurns, turns.get(0));
  }

  private static Stream<Arguments> paramsForDividingTurnsTest() {
    return Stream.of(
        Arguments.of("left", List.of(TurnDirection.LEFT)),
        Arguments.of("left;through", List.of(TurnDirection.LEFT, TurnDirection.THROUGH)),
        Arguments.of("left;through;right",
            List.of(TurnDirection.LEFT, TurnDirection.THROUGH, TurnDirection.RIGHT)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("paramsForDividingLanesTest")
  public void dividingLanes(String value, int lanesNo) {
    // given
    WayData wayData = Mockito.mock(WayData.class, Mockito.RETURNS_DEEP_STUBS);
    Map<String, String> tags = Map.of("turn:lanes", value);

    // when
    Mockito.when(wayData.getTags()).thenReturn(tags);
    Mockito.when(wayData.isTagsInOppositeMeaning()).thenReturn(false);

    // then
    List<List<TurnDirection>> turns = processor.getTurnDirectionsFromTags(wayData);
    Assertions.assertEquals(lanesNo, turns.size());
  }

  private static Stream<Arguments> paramsForDividingLanesTest() {
    return Stream.of(
        Arguments.of("left", 1),
        Arguments.of("left|through", 2),
        Arguments.of("left|||", 4)
    );
  }
}
