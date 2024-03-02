package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StandardTIDeterminerTest {
  private final StandardTIDeterminer determiner = new StandardTIDeterminer();

  @ParameterizedTest
  @MethodSource("provideParamsForTICheck")
  public void check(Map<String, String> tags, boolean result) {
    // given

    // when

    // then
    Assertions.assertEquals(result, determiner.checkFromTags(tags));
  }

  private static Stream<Arguments> provideParamsForTICheck() {
    return Stream.of(
        Arguments.of(Map.of("highway", "traffic_signals"), true),
        Arguments.of(Map.of("highway", "primary"), false),
        Arguments.of(Map.of("way", "secondary"), false),
        Arguments.of(Map.of(), false)
    );
  }
}
