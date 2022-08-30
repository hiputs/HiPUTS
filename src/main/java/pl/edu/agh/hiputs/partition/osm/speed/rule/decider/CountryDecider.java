package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.resolver.Resolver;

@Service
@Order(2)
@RequiredArgsConstructor
public class CountryDecider implements Decider{
  private final static String countryKey = "addr:country";
  private final Resolver defaultCountryResolver;

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    List<String> foundCountries = getNodesIds(speedDataHandler.getOsmWay()).stream()
        .filter(id -> speedDataHandler.getMapOfOsmNodes().containsKey(id))
        .map(id -> speedDataHandler.getMapOfOsmNodes().get(id))
        .filter(node -> OsmModelUtil.getTagsAsMap(node).containsKey(countryKey))
        .map(node -> OsmModelUtil.getTagsAsMap(node).get(countryKey))
        .map(String::toUpperCase)
        .collect(Collectors.toList());

    if(foundCountries.isEmpty()) {
      speedDataHandler.setCountry(defaultCountryResolver.getValue());
    } else {
      speedDataHandler.setCountry(foundCountries.get(0));
    }
  }

  private List<Long> getNodesIds(OsmWay way) {
    List<Long> ids = new ArrayList<>();
    for(int i=0; i<way.getNumberOfNodes(); i++) {
      ids.add(way.getNodeId(i));
    }

    return ids;
  }
}
