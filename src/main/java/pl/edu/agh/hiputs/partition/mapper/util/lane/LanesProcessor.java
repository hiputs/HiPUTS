package pl.edu.agh.hiputs.partition.mapper.util.lane;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.partition.model.LaneData;

public interface LanesProcessor {

  Map<RelativeDirection, List<LaneData>> getDataForEachDirectionFromTags(Map<String, String> tags);

}
