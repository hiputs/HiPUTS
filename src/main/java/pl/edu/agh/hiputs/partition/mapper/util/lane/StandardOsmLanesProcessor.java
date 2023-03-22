package pl.edu.agh.hiputs.partition.mapper.util.lane;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.oneway.OneWayProcessor;
import pl.edu.agh.hiputs.partition.model.LaneData;

@Service
@RequiredArgsConstructor
public class StandardOsmLanesProcessor implements LanesProcessor {
  private final static String LANES_TOTAL_KEY = "lanes";
  private final static String LANES_FORWARD_KEY = "lanes:forward";
  private final static String LANES_BACKWARD_KEY = "lanes:backward";
  private final static String LANES_BOTH_WAYS_KEY = "lanes:both_ways";

  private final OneWayProcessor oneWayProcessor;

  @Override
  public Map<RelativeDirection, List<LaneData>> getDataForEachDirectionFromTags(Map<String, String> tags) {
    boolean isOneway = oneWayProcessor.checkFromTags(tags);

    if (tags.containsKey(LANES_TOTAL_KEY)) {
      if (tags.containsKey(LANES_FORWARD_KEY) && tags.containsKey(LANES_BACKWARD_KEY) && tags.containsKey(LANES_BOTH_WAYS_KEY)) {
        // 1. Lanes number with forward, backward and both_ways => equal assigning both_ways number of lanes to roads
        return Map.of(
            RelativeDirection.SAME, createNoLanes(
                Integer.parseInt(tags.get(LANES_FORWARD_KEY) + Integer.parseInt(tags.get(LANES_BOTH_WAYS_KEY)))
            ),
            RelativeDirection.OPPOSITE, createNoLanes(
                Integer.parseInt(tags.get(LANES_BACKWARD_KEY) + Integer.parseInt(tags.get(LANES_BOTH_WAYS_KEY)))
            ));
      }
      else if (tags.containsKey(LANES_FORWARD_KEY) && tags.containsKey(LANES_BACKWARD_KEY)) {
        // 2. Lanes number with forward and backward => trivial
        return Map.of(RelativeDirection.SAME, createNoLanes(Integer.parseInt(tags.get(LANES_FORWARD_KEY))),
            RelativeDirection.OPPOSITE, createNoLanes(Integer.parseInt(tags.get(LANES_BACKWARD_KEY))));
      }
      else if (tags.containsKey(LANES_FORWARD_KEY)) {
        // 3. Lanes number with forward only => assigning forward to SAME total-forward to OPPOSITE
        return Map.of(RelativeDirection.SAME, createNoLanes(Integer.parseInt(tags.get(LANES_FORWARD_KEY))),
            RelativeDirection.OPPOSITE, createNoLanes(
                Integer.parseInt(tags.get(LANES_TOTAL_KEY)) - Integer.parseInt(tags.get(LANES_FORWARD_KEY))
            ));
      }
      else if (tags.containsKey(LANES_BACKWARD_KEY)) {
        // 4. Lanes number with backward only => assigning backward to OPPOSITE total-backward to SAME
        return Map.of(RelativeDirection.SAME, createNoLanes(
            Integer.parseInt(tags.get(LANES_TOTAL_KEY)) - Integer.parseInt(tags.get(LANES_BACKWARD_KEY))),
            RelativeDirection.OPPOSITE, createNoLanes(Integer.parseInt(tags.get(LANES_BACKWARD_KEY))
            ));
      }
      else {
        // 5. Only lanes number => assigning it to road if oneWay or equally dividing into two roads if not
        return isOneway ? Map.of(RelativeDirection.SAME, createNoLanes(Integer.parseInt(tags.get(LANES_TOTAL_KEY)))) :
            Map.of(RelativeDirection.SAME, createNoLanes(Integer.parseInt(tags.get(LANES_TOTAL_KEY)) / 2),
                RelativeDirection.OPPOSITE, createNoLanes(Integer.parseInt(tags.get(LANES_TOTAL_KEY)) / 2));
      }
    } else {
      // 6. Lack of required data => one-lane road.
      return isOneway ? Map.of(RelativeDirection.SAME, createNoLanes(1)) :
          Map.of(RelativeDirection.SAME, createNoLanes(1), RelativeDirection.OPPOSITE, createNoLanes(1));
    }
  }

  private List<LaneData> createNoLanes(int number) {
    return IntStream.rangeClosed(1, number)
        .mapToObj(num -> new LaneData())
        .toList();
  }
}
