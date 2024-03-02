package pl.edu.agh.hiputs.partition.osm.speed.rule.resolver;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.countryKeyInTags;
import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.defaultSpeedLimitRecordName;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DefaultCountryResolver implements Resolver{
  private String country;

  @Override
  public void findValue(List<OsmNode> nodes) {
     country = nodes.stream()
         .filter(node -> OsmModelUtil.getTagsAsMap(node).containsKey(countryKeyInTags))
         .map(node -> OsmModelUtil.getTagsAsMap(node).get(countryKeyInTags))
         .map(String::toUpperCase)
         .collect(Collectors.groupingBy(country -> country, Collectors.counting()))
         .entrySet().stream()
         .max(Entry.comparingByValue())
         .map(Entry::getKey)
         .orElse(defaultSpeedLimitRecordName);
  }

  @Override
  public String getValue() {
    return country;
  }
}
