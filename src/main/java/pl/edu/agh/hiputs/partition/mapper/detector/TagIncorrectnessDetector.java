package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.TagCorrector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge.EdgeIssuesFinder;
import pl.edu.agh.hiputs.partition.mapper.detector.util.tag.node.NodeIssuesFinder;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(1)
@RequiredArgsConstructor
public class TagIncorrectnessDetector implements Detector {

  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final List<EdgeIssuesFinder> edgeFinders;
  private final List<NodeIssuesFinder> nodeFinders;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    List<Pair<String, List<Edge<JunctionData, WayData>>>> edgeFindersResult = edgeFinders.stream()
        .map(edgeFinder -> edgeFinder.lookup(graph))
        .filter(pair -> !pair.getRight().isEmpty())
        .toList();

    List<Pair<String, List<Node<JunctionData, WayData>>>> nodeFindersResult = nodeFinders.stream()
        .map(nodeFinder -> nodeFinder.lookup(graph))
        .filter(pair -> !pair.getRight().isEmpty())
        .toList();

    StandardDetectorContext context = new StandardDetectorContext();
    if (!edgeFindersResult.isEmpty() || !nodeFindersResult.isEmpty()) {
      context.setDetectionReport(String.format("%s - found tagging issues:\n%s\n%s", getClass().getSimpleName(),
          formatReportForEdges(edgeFindersResult), formatReportForNodes(nodeFindersResult)));

      context.setPreparedCorrector(new TagCorrector(edgeFindersResult, nodeFindersResult));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForEdges(List<Pair<String, List<Edge<JunctionData, WayData>>>> edgeFindersResult) {
    StringBuilder edgesReportBuilder = new StringBuilder();
    edgeFindersResult.forEach(edgeFinderResult -> edgesReportBuilder.append(
        String.format("\"%s\": [\n%s]\n", edgeFinderResult.getLeft(), edgeFinderResult.getRight()
            .stream()
            .map(Edge::getId)
            .reduce("", (current, next) -> current + String.format("\t%s,\n", next)))));

    return String.format("Edges:\n%s", edgesReportBuilder);
  }

  private String formatReportForNodes(List<Pair<String, List<Node<JunctionData, WayData>>>> nodeFindersResult) {
    StringBuilder nodesReportBuilder = new StringBuilder();
    nodeFindersResult.forEach(nodeFinderResult -> nodesReportBuilder.append(
        String.format("\"%s\": [\n%s]\n", nodeFinderResult.getLeft(), nodeFinderResult.getRight()
            .stream()
            .map(Node::getId)
            .reduce("", (current, next) -> current + String.format("\t%s,\n", next)))));

    return String.format("Nodes:\n%s", nodesReportBuilder);
  }
}
