package pl.edu.agh.hiputs.partition.service;

import static java.lang.Math.sqrt;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.utils.MinMaxAcc;

@Primary // todo - used for scalabity tests - for normal use mark QuadTreeMap... as @Primary
@Service
public class RectangleMapFragmentPartitioner extends QuadTreeMapFragmentPartitioner {

  public RectangleMapFragmentPartitioner(ConfigurationService configurationService) {
    super(configurationService);
  }

  @Override
  public Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph) {
    return partition(graph, configurationService.getConfiguration().getWorkerCount());
  }

  private Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph,
      int partsCount) {
    if (partsCount == 1) {
      return List.of(graph);
    } else {
      calculateLatLonData(graph);
      MinMaxAcc widthMinMax = getMinMaxAcc(graph, PatchData::getMinMaxLon);
      MinMaxAcc heightMinMax = getMinMaxAcc(graph, PatchData::getMinMaxLat);

      Pair<Integer, Integer> divisors = calc_dims_divisors(partsCount);

      Pair<Function<PatchData, Optional<Double>>, Function<PatchData, Optional<Double>>> fun =
          widthMinMax.getRange() > heightMinMax.getRange() ? Pair.of(PatchData::getAvgLon, PatchData::getAvgLat)
              : Pair.of(PatchData::getAvgLat, PatchData::getAvgLon);

      List<Graph<PatchData, PatchConnectionData>> subParts =
          divide(graph, divisors.getLeft(), Math.max(heightMinMax.getRange(), widthMinMax.getRange()), fun.getLeft());

      return subParts.stream()
          .map(subGraph -> divide(subGraph, divisors.getRight(),
              Math.min(heightMinMax.getRange(), widthMinMax.getRange()), fun.getRight()))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    }
  }

  private MinMaxAcc getMinMaxAcc(Graph<PatchData, PatchConnectionData> graph, Function<PatchData, MinMaxAcc> getter) {
    MinMaxAcc minMax = new MinMaxAcc();
    graph.getNodes()
        .values()
        .stream()
        .map(patchNode -> getter.apply(patchNode.getData()).getMin())
        .forEach(minMax::accept);
    graph.getNodes()
        .values()
        .stream()
        .map(patchNode -> getter.apply(patchNode.getData()).getMax())
        .forEach(minMax::accept);
    return minMax;
  }

  private List<Graph<PatchData, PatchConnectionData>> divide(Graph<PatchData, PatchConnectionData> graph,
      int partsCount, double range, Function<PatchData, Optional<Double>> getter) {

    List<Graph<PatchData, PatchConnectionData>> dividedParts = new LinkedList<>();
    double partition_range = range / partsCount;

    for (int i = 0; i < partsCount; i++) {
      final int finalI = i;
      List<Node<PatchData, PatchConnectionData>> graphNodes = graph.getNodes()
          .values()
          .stream()
          .filter(patchNode -> (getter.apply(patchNode.getData()).get() >= partition_range * finalI
              && getter.apply(patchNode.getData()).get() < partition_range * (finalI + 1)))
          .toList();

      GraphBuilder<PatchData, PatchConnectionData> graphBuilder = new GraphBuilder<>();
      graphNodes.forEach(graphBuilder::addNode);
      graphNodes.forEach(patchNode -> patchNode.getIncomingEdges().forEach(graphBuilder::addEdge));
      dividedParts.add(graphBuilder.build());
    }

    return dividedParts;
  }

  private Pair<Integer, Integer> calc_dims_divisors(int parts) {
    int square = (int) sqrt(parts);
    int x = 1;

    if (parts / square == parts / square) {
      x = square;
    } else if (parts / (square + 1) == parts / (square + 1)) {
      x = square + 1;
    } else if (parts / (square - 1) == parts / (square - 1)) {
      x = square - 1;
    }

    return Pair.of(Math.max(x, parts / x), Math.min(x, parts / x));
  }
}
