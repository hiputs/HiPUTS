package pl.edu.agh.hiputs.partition.mapper.util.turn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.WayData;

@Service
public class StandardOsmTurnProcessor implements TurnProcessor {
  private final static String TURN_KEY = "turn";
  private final static String TURN_FORWARD_KEY = TURN_KEY + ":forward";
  private final static String TURN_BACKWARD_KEY = TURN_KEY + ":backward";
  private final static String TURN_BOTH_WAYS_KEY = TURN_KEY + ":both_ways";
  private final static String TURN_LANE_KEY = TURN_KEY + ":lanes";
  private final static String TURN_LANE_FORWARD_KEY = TURN_LANE_KEY + ":forward";
  private final static String TURN_LANE_BACKWARD_KEY = TURN_LANE_KEY + ":backward";
  private final static String TURN_LANE_BOTH_WAYS_KEY = TURN_LANE_KEY + ":both_ways";

  private final static String NONE_TURN_INDICATOR = "none";

  private final static String LANES_SEPARATOR = "\\|";
  private final static String TURN_SEPARATOR = ";";

  @Override
  public List<List<TurnDirection>> getTurnDirectionsFromTags(WayData wayData) {
    if (wayData.getTags().containsKey(TURN_KEY)) {
      return wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_KEY), wayData.getLanes().size());
    }
    else if (wayData.getTags().containsKey(TURN_BOTH_WAYS_KEY)) {
      return convertRoadTagToTurns(wayData.getTags().get(TURN_KEY), wayData.getLanes().size());
    }
    else if (wayData.getTags().containsKey(TURN_FORWARD_KEY) && wayData.getTags().containsKey(TURN_BACKWARD_KEY)) {
      return wayData.isTagsInOppositeMeaning() ?
          convertRoadTagToTurns(wayData.getTags().get(TURN_BACKWARD_KEY), wayData.getLanes().size()) :
          convertRoadTagToTurns(wayData.getTags().get(TURN_FORWARD_KEY), wayData.getLanes().size());
    }
    else if (wayData.getTags().containsKey(TURN_FORWARD_KEY)) {
      return wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_FORWARD_KEY), wayData.getLanes().size());
    }
    else if (wayData.getTags().containsKey(TURN_BACKWARD_KEY)) {
      return !wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_BACKWARD_KEY), wayData.getLanes().size());
    }
    else if (wayData.getTags().containsKey(TURN_LANE_KEY)) {
      return wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_KEY));
    }
    else if (wayData.getTags().containsKey(TURN_LANE_BOTH_WAYS_KEY)) {
      return convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_KEY));
    }
    else if (wayData.getTags().containsKey(TURN_LANE_FORWARD_KEY) && wayData.getTags().containsKey(TURN_LANE_BACKWARD_KEY)) {
      return wayData.isTagsInOppositeMeaning() ?
          convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_BACKWARD_KEY)) :
          convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_FORWARD_KEY));
    }
    else if (wayData.getTags().containsKey(TURN_LANE_FORWARD_KEY)) {
      return wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_FORWARD_KEY));
    }
    else if (wayData.getTags().containsKey(TURN_LANE_BACKWARD_KEY)) {
      return !wayData.isTagsInOppositeMeaning() ? Collections.emptyList() :
          convertRoadTagToTurns(wayData.getTags().get(TURN_LANE_BACKWARD_KEY));
    }
    else {
      return Collections.emptyList();
    }
  }

  private List<List<TurnDirection>> convertRoadTagToTurns(String entry) {
    return Stream.of(entry)
        .flatMap(string -> Arrays.stream(string.split(LANES_SEPARATOR, -1)))
        .map(this::convertLaneTagToTurns)
        .toList();
  }

  private List<List<TurnDirection>> convertRoadTagToTurns(String entry, int noLanes) {
    return convertRoadTagToTurns(IntStream.rangeClosed(1, noLanes)
        .mapToObj(num -> entry)
        .collect(Collectors.joining(LANES_SEPARATOR))
    );
  }

  private List<TurnDirection> convertLaneTagToTurns(String entry) {
    return Stream.of(entry)
        .map(string -> StringUtils.isBlank(entry) ? NONE_TURN_INDICATOR : string)
        .flatMap(string -> Arrays.stream(string.split(TURN_SEPARATOR)))
        .map(String::toUpperCase)
        .map(TurnDirection::valueOf)
        .toList();
  }
}
