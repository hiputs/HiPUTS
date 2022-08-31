package pl.edu.agh.hiputs.partition.osm.speed.rule.engine;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.rule.decider.Decider;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.resolver.Resolver;

@Service
@RequiredArgsConstructor
public class RuleEngineImpl implements RuleEngine{
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

    ways.forEach(way -> {
      SpeedResultHandler handler = new SpeedResultHandler();
      handler.setMapOfOsmNodes(nodesAsMap);
      handler.setOsmWay(way);

      deciders.forEach(decider -> decider.decideAboutValue(handler));

      // @TODO here speed value must be set
      System.out.println(handler.getResultSpeed());
    });
  }
}
