package pl.edu.agh.hiputs.partition.mapper.verifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.verifier.component.Requirement;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class StandardRequirementsVerifier implements RequirementsVerifier {

  private final List<Requirement> requirements;

  @Override
  public void verifyAll(Graph<JunctionData, WayData> graph) {
    Map<Boolean, List<String>> results = requirements.stream()
        .map(requirement -> Pair.of(requirement.isSatisfying(graph), requirement.getName()))
        .collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));

    System.out.printf("========== Requirements verifying results ========== \n%s",
        formatReportForVerifyingResults(results));
  }

  private String formatReportForVerifyingResults(Map<Boolean, List<String>> results) {
    StringBuilder resultsStringBuilder = new StringBuilder();

    if (results.containsKey(true)) {
      resultsStringBuilder.append("SATISFIED:\n");
      results.get(true).forEach(requirement -> resultsStringBuilder.append(requirement).append("\n"));
    }

    if (results.containsKey(false)) {
      resultsStringBuilder.append("NOT SATISFIED:\n");
      results.get(false).forEach(requirement -> resultsStringBuilder.append(requirement).append("\n"));
    }

    return resultsStringBuilder.append("\n").toString();
  }
}
