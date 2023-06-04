package pl.edu.agh.hiputs.partition.mapper.filter;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration.TagEntry;
import pl.edu.agh.hiputs.partition.osm.OsmGraph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(3)
@RequiredArgsConstructor
public class SelectionFilter implements Filter{
  private final ModelConfigurationService modelConfigService;

  @Override
  public OsmGraph filter(OsmGraph osmGraph) {
    List<OsmWay> filteredWays = osmGraph.getWays();

    filteredWays = getRequestedEntities(
        Arrays.stream(modelConfigService.getModelConfig().getWayConditions().getMandatoryTagEntries())
            .collect(Collectors.toMap(TagEntry::getKey, TagEntry::getValue)),
        filteredWays
    );

    filteredWays = filterOutWrongEntities(
        Arrays.stream(modelConfigService.getModelConfig().getWayConditions().getProhibitedTagEntries())
            .collect(Collectors.toMap(TagEntry::getKey, TagEntry::getValue)),
        filteredWays
    );

    List<OsmNode> filteredNodes = osmGraph.getNodes();

    filteredNodes = getRequestedEntities(
        Arrays.stream(modelConfigService.getModelConfig().getNodeConditions().getMandatoryTagEntries())
            .collect(Collectors.toMap(TagEntry::getKey, TagEntry::getValue)),
        filteredNodes
    );

    filteredNodes = filterOutWrongEntities(
        Arrays.stream(modelConfigService.getModelConfig().getNodeConditions().getProhibitedTagEntries())
            .collect(Collectors.toMap(TagEntry::getKey, TagEntry::getValue)),
        filteredNodes
    );

    return new OsmGraph(filteredNodes, filteredWays);
  }

  private <T extends OsmEntity> List<T> getRequestedEntities(Map<String, String> tagEntries, List<T> entities) {
    if (tagEntries.isEmpty()) {
      return entities;
    }

    return entities.stream()
        .filter(osmEntity -> {
          Map<String, String> actualTagMap = OsmModelUtil.getTagsAsMap(osmEntity);
          return tagEntries.entrySet().stream().allMatch(entry ->
              actualTagMap.containsKey(entry.getKey()) && actualTagMap.get(entry.getKey()).equals(entry.getValue()));
        }).toList();
  }

  private <T extends OsmEntity> List<T> filterOutWrongEntities(Map<String, String> tagEntries, List<T> entities) {
    if (tagEntries.isEmpty()) {
      return entities;
    }

    return entities.stream()
        .filter(osmEntity -> {
          Map<String, String> actualTagMap = OsmModelUtil.getTagsAsMap(osmEntity);
          return tagEntries.entrySet().stream().noneMatch(entry ->
              actualTagMap.containsKey(entry.getKey()) && actualTagMap.get(entry.getKey()).equals(entry.getValue()));
        }).toList();
  }
}
