package pl.edu.agh.hiputs.partition.osm.speed.rule.engine;

import static pl.edu.agh.hiputs.partition.osm.speed.CommonConstants.maxSpeedKeyInTags;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.Decider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.osm.speed.rule.resolver.Resolver;
import pl.edu.agh.hiputs.partition.osm.speed.rule.transform.TagTransformer;

@Service
@RequiredArgsConstructor
public class RuleEngineImpl implements RuleEngine{
  private final TagTransformer speedTagTransformer;
  private final Resolver defaultCountryResolver;
  private final List<Decider> deciders;

  @Override
  public void findDefaultCountry(List<OsmNode> nodes) {
    defaultCountryResolver.findValue(nodes);
  }

  @Override
  public void validateSpeedLimits(List<OsmNode> nodesOnWays, List<OsmWay> ways) {
    Map<Long, OsmNode> nodesAsMap = nodesOnWays.stream()
        .collect(Collectors.toMap(OsmNode::getId, Function.identity()));

    List<OsmWay> waysToRemove = new ArrayList<>();
    List<OsmWay> transformedWays = new ArrayList<>();

    ways.forEach(way -> {
      if (!OsmModelUtil.getTagsAsMap(way).containsKey(maxSpeedKeyInTags)) {
        SpeedResultHandler handler = new SpeedResultHandler();
        handler.setMapOfOsmNodes(nodesAsMap);
        handler.setOsmWay(way);

        deciders.forEach(decider -> decider.decideAboutValue(handler));

        waysToRemove.add(way);
        if (handler.getTypeOfRoad() != TypeOfRoad.NotClassified) {
          transformedWays.add(speedTagTransformer.replaceValue(way, handler.getResultSpeed()));
        }
      }
    });

    ways.removeAll(waysToRemove);
    ways.addAll(transformedWays);
  }
}
