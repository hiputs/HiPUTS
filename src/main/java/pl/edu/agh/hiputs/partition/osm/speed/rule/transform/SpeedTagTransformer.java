package pl.edu.agh.hiputs.partition.osm.speed.rule.transform;

import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpeedTagTransformer implements TagTransformer{
  private static final String maxSpeedKeyInTags = "maxspeed";

  @Override
  public OsmWay replaceValue(OsmWay way, String value) {
    List<OsmTag> tagsList = OsmModelUtil.getTagsAsList(way).stream()
        .filter(tag -> !tag.getKey().equals(maxSpeedKeyInTags))
        .collect(Collectors.toList());

    tagsList.add(new Tag(maxSpeedKeyInTags, value));

    return new Way(way.getId(), OsmModelUtil.nodesAsList(way), tagsList, way.getMetadata());
  }
}
