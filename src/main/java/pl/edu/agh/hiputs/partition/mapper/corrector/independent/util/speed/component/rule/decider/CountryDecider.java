package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import static pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.SpeedConstants.countryKeyInTags;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver.CountryResolver;

@Service
@Order(2)
@RequiredArgsConstructor
public class CountryDecider implements Decider{
  private final CountryResolver defaultCountryResolver;

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    speedDataHandler.setCountry(Stream.of(speedDataHandler.getEdge().getData().getTags(),
            speedDataHandler.getEdge().getSource().getData().getTags(),
            speedDataHandler.getEdge().getTarget().getData().getTags())
        .filter(map -> map.containsKey(countryKeyInTags))
        .map(map -> map.get(countryKeyInTags))
        .map(String::toUpperCase)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet().stream()
        .max(Entry.comparingByValue())
        .map(Entry::getKey)
        .orElse(defaultCountryResolver.getCountry()));
  }
}

