package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import static pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.SpeedConstants.maxSpeedKeyInTags;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.RuleEngine;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver.CountryResolver;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(3)
@RequiredArgsConstructor
public class SpeedLimitsCorrector implements Corrector {
  private final static String knotsSuffix = "knots";
  private final static String mphSuffix = "mph";
  private final static double knotsToKmhMultiplier = 1.852;
  private final static double mphToKmhMultiplier = 1.609344;
  private final CountryResolver countryResolver;
  private final RuleEngine ruleEngine;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    countryResolver.deduceCountry(graph);

    graph.getEdges().values().forEach(edge -> {
      WayData wayData = edge.getData();

      if (wayData.getTags().containsKey(maxSpeedKeyInTags)) {
        try {
          String speedValue = wayData.getTags().get(maxSpeedKeyInTags);
          if (speedValue.endsWith(mphSuffix)) {
            wayData.setMaxSpeed(
                (int)(Integer.parseInt(speedValue.replace(mphSuffix, "").trim()) * mphToKmhMultiplier));
          } else if (speedValue.endsWith(knotsSuffix)) {
            wayData.setMaxSpeed(
                (int)(Integer.parseInt(speedValue.replace(knotsSuffix, "").trim()) * knotsToKmhMultiplier));
          } else {
            wayData.setMaxSpeed(Integer.parseInt(wayData.getTags().get(maxSpeedKeyInTags)));
          }
        } catch (NumberFormatException exception) {
          wayData.setMaxSpeed(ruleEngine.processWay(edge));
        }
      } else {
        wayData.setMaxSpeed(ruleEngine.processWay(edge));
      }
    });

    return graph;
  }
}
