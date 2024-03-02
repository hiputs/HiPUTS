package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver;

import static pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.SpeedConstants.countryKeyInTags;
import static pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.SpeedConstants.defaultSpeedLimitRecordName;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
public class StandardCountryResolver implements CountryResolver {

  @Getter // implementing done by getter
  private String country;

  @Override
  public void deduceCountry(Graph<JunctionData, WayData> graph) {
    country = graph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().getTags().containsKey(countryKeyInTags))
        .map(node -> node.getData().getTags().get(countryKeyInTags))
        .map(String::toUpperCase)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Entry.comparingByValue())
        .map(Entry::getKey)
        .or(() -> graph.getEdges()
            .values()
            .stream()
            .filter(edge -> edge.getData().getTags().containsKey(countryKeyInTags))
            .map(edge -> edge.getData().getTags().get(countryKeyInTags))
            .map(String::toUpperCase)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet()
            .stream()
            .max(Entry.comparingByValue())
            .map(Entry::getKey))
        .orElse(defaultSpeedLimitRecordName);
  }
}
