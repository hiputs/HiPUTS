package pl.edu.agh.hiputs.partition.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.utils.MinMaxAcc;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "map-fragment-partitioner", havingValue = "quadTree")
public class QuadTreeMapFragmentPartitioner implements MapFragmentPartitioner {

  protected final Configuration configuration;

  @Override
  public Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph) {
    return partition(graph, configuration.getWorkerCount());
  }

  private Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph, int partsCount) {
    if (partsCount == 1) {
      return List.of(graph);
    } else {
      int leftPartsCount = partsCount / 2;
      int rightPartsCount = partsCount - leftPartsCount;
      Pair<Graph<PatchData, PatchConnectionData>, Graph<PatchData, PatchConnectionData>> bisectionResult =
          bisect(graph);
      return Stream.of(partition(bisectionResult.getLeft(), leftPartsCount),
          partition(bisectionResult.getRight(), rightPartsCount))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    }
  }

  private Pair<Graph<PatchData, PatchConnectionData>, Graph<PatchData, PatchConnectionData>> bisect(
      Graph<PatchData, PatchConnectionData> graph) {
    //calculate averages
    calculateLatLonData(graph);

    MinMaxAcc widthMinMax = getMinMaxAcc(graph, PatchData::getAvgLon);
    MinMaxAcc heightMinMax = getMinMaxAcc(graph, PatchData::getAvgLat);

    return widthMinMax.getRange() > heightMinMax.getRange() ? bisectVertically(graph, widthMinMax.getMiddle())
        : bisectHorizontally(graph, heightMinMax.getMiddle());
  }

  private MinMaxAcc getMinMaxAcc(Graph<PatchData, PatchConnectionData> graph,
      Function<PatchData, Optional<Double>> getter) {
    MinMaxAcc minMax = new MinMaxAcc();
    graph.getNodes()
        .values()
        .stream()
        .map(patchNode -> getter.apply(patchNode.getData()).get())
        .forEach(minMax::accept);
    return minMax;
  }

  protected void calculateLatLonData(Graph<PatchData, PatchConnectionData> graph) {
    graph.getNodes().values().forEach(patchNode -> {
      Pair<Optional<Double>, MinMaxAcc> latData =
          calculateAverageForPatchNodeAttribute(patchNode, JunctionData::getLat);
      Pair<Optional<Double>, MinMaxAcc> lonData =
          calculateAverageForPatchNodeAttribute(patchNode, JunctionData::getLon);
      patchNode.getData().setAvgLat(latData.getLeft());
      patchNode.getData().setMinMaxLat(latData.getRight());
      patchNode.getData().setAvgLon(lonData.getLeft());
      patchNode.getData().setMinMaxLon(lonData.getRight());
    });
  }

  private Pair<Graph<PatchData, PatchConnectionData>, Graph<PatchData, PatchConnectionData>> bisectHorizontally(Graph<PatchData, PatchConnectionData> graph, Double middle) {
    return bisectByPatchDataAttribute(graph, PatchData::getAvgLat, middle);
  }

  private Pair<Graph<PatchData, PatchConnectionData>, Graph<PatchData, PatchConnectionData>> bisectVertically(Graph<PatchData, PatchConnectionData> graph, Double middle) {
    return bisectByPatchDataAttribute(graph, PatchData::getAvgLon, middle);
  }

  private Pair<Graph<PatchData, PatchConnectionData>, Graph<PatchData, PatchConnectionData>> bisectByPatchDataAttribute(Graph<PatchData, PatchConnectionData> graph, Function<PatchData, Optional<Double>> getter,  Double middle) {
    List<Node<PatchData, PatchConnectionData>> leftGraphNodes = graph.getNodes()
        .values()
        .stream()
        .filter(patchNode -> getter.apply(patchNode.getData()).get() <= middle)
        .toList();

    List<Node<PatchData, PatchConnectionData>> rightGraphNodes = graph.getNodes()
        .values()
        .stream()
        .filter(patchNode -> getter.apply(patchNode.getData()).get() > middle)
        .toList();

    GraphBuilder<PatchData, PatchConnectionData> leftGraphBuilder = new GraphBuilder<>();
    GraphBuilder<PatchData, PatchConnectionData> rightGraphBuilder = new GraphBuilder<>();

    leftGraphNodes.forEach(leftGraphBuilder::addNode);
    rightGraphNodes.forEach(rightGraphBuilder::addNode);

    leftGraphNodes.forEach(patchNode -> patchNode.getIncomingEdges().forEach(leftGraphBuilder::addEdge));
    rightGraphNodes.forEach(patchNode -> patchNode.getIncomingEdges().forEach(rightGraphBuilder::addEdge));

    return Pair.of(leftGraphBuilder.build(), rightGraphBuilder.build());
  }

  private Pair<Optional<Double>, MinMaxAcc> calculateAverageForPatchNodeAttribute(
      Node<PatchData, PatchConnectionData> patchNode, Function<JunctionData, Double> getter) {
    List<Double> values = patchNode.getData()
        .getGraphInsidePatch()
        .getEdges()
        .values()
        .stream()
        .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
        .map(node -> getter.apply(node.getData()))
        .toList();
    long nodesCount = values.size();

    Double res = values.stream()
        .reduce(Double::sum)
        .map(sum -> sum / nodesCount)
        .orElseThrow(() -> new RuntimeException("Cannot calculate average value of junction data attribute"));

    MinMaxAcc minMax = new MinMaxAcc();
    values.forEach(minMax::accept);

    return Pair.of(Optional.of(res), minMax);
  }

}
