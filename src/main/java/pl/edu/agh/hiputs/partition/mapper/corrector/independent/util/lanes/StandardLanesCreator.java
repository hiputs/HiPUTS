package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.lanes;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.oneway.OneWayProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@RequiredArgsConstructor
public class StandardLanesCreator implements LanesCreator{
  private final static String LANES_TOTAL_KEY = "lanes";
  private final static String LANES_FORWARD_KEY = LANES_TOTAL_KEY + ":forward";
  private final static String LANES_BACKWARD_KEY = LANES_TOTAL_KEY + ":backward";
  private final static String LANES_BOTH_WAYS_KEY = LANES_TOTAL_KEY + ":both_ways";

  private final OneWayProcessor oneWayProcessor;

  @Override
  public void deduceAndCreate(Edge<JunctionData, WayData> edge) {
    edge.getData().getLanes().addAll(createLanesForEdge(edge.getData()));
  }

  private List<LaneData> createLanesForEdge(WayData wayData) {
    if (wayData.getTags().containsKey(LANES_TOTAL_KEY)) {
      if (wayData.getTags().containsKey(LANES_FORWARD_KEY) &&
          wayData.getTags().containsKey(LANES_BACKWARD_KEY) &&
          wayData.getTags().containsKey(LANES_BOTH_WAYS_KEY)) {
        // 1. Lanes number with forward, backward and both_ways => equal assigning both_ways number of lanes to roads
        return wayData.isTagsInOppositeMeaning() ?
            createNoLanes(retrieveLanesNo(wayData, LANES_BACKWARD_KEY) + retrieveLanesNo(wayData, LANES_BOTH_WAYS_KEY)) :
            createNoLanes(retrieveLanesNo(wayData, LANES_FORWARD_KEY) + retrieveLanesNo(wayData, LANES_BOTH_WAYS_KEY));
      }
      else if (wayData.getTags().containsKey(LANES_FORWARD_KEY) && wayData.getTags().containsKey(LANES_BACKWARD_KEY)) {
        // 2. Lanes number with forward and backward => trivial
        return wayData.isTagsInOppositeMeaning() ?
            createNoLanes(retrieveLanesNo(wayData, LANES_BACKWARD_KEY)) :
            createNoLanes(retrieveLanesNo(wayData, LANES_FORWARD_KEY));
      }
      else if (wayData.getTags().containsKey(LANES_FORWARD_KEY)) {
        // 3. Lanes number with forward only => assigning forward when the same road direction or difference if not
        return wayData.isTagsInOppositeMeaning() ?
            createNoLanes(retrieveLanesNo(wayData, LANES_TOTAL_KEY) - retrieveLanesNo(wayData, LANES_FORWARD_KEY)) :
            createNoLanes(retrieveLanesNo(wayData, LANES_FORWARD_KEY));
      }
      else if (wayData.getTags().containsKey(LANES_BACKWARD_KEY)) {
        // 4. Lanes number with backward only => assigning different when the same road direction or backward if not
        return wayData.isTagsInOppositeMeaning() ?
            createNoLanes(retrieveLanesNo(wayData, LANES_BACKWARD_KEY)) :
            createNoLanes(retrieveLanesNo(wayData, LANES_TOTAL_KEY) - retrieveLanesNo(wayData, LANES_BACKWARD_KEY));
      }
      else {
        if (oneWayProcessor.checkFromTags(wayData.getTags())) {
          // 5. Only lanes number => assigning it to road if oneWay
          return createNoLanes(retrieveLanesNo(wayData, LANES_TOTAL_KEY));
        }
        else {
          // 6. Only lanes number => equally dividing into two roads if not oneWay
          return createNoLanes(Math.max(retrieveLanesNo(wayData, LANES_TOTAL_KEY) / 2, 1));
        }
      }
    } else {
      // 7. Lack of required data => one-lane road.
      return createNoLanes(1);
    }
  }

  private int retrieveLanesNo(WayData wayData, String key) {
    try {
      return Integer.parseInt(wayData.getTags().get(key));
    } catch (NumberFormatException e) {
      try {
        return (int) Math.ceil(Double.parseDouble(wayData.getTags().get(key)));
      } catch (NumberFormatException | ClassCastException ex) {
        return 1;
      }
    }
  }

  private List<LaneData> createNoLanes(int number) {
    return IntStream.rangeClosed(1, Math.max(1, number))
        .mapToObj(num -> LaneData.builder().build())
        .toList();
  }
}
