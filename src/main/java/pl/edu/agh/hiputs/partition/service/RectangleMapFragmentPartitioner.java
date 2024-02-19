package pl.edu.agh.hiputs.partition.service;

import static java.lang.Math.sqrt;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.utils.MinMaxAcc;

// @Primary // todo - used for scalability tests - for normal use mark QuadTreeMap... as @Primary
@Service
public class RectangleMapFragmentPartitioner extends QuadTreeMapFragmentPartitioner {

  public RectangleMapFragmentPartitioner(Configuration configuration) {
    super(configuration);
  }

  @Override
  public Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph) {
    return partition(graph, ConfigurationService.getConfiguration().getWorkerCount());
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

      Function<PatchData, Optional<Double>> fun1, fun2;
      MinMaxAcc minMax1, minMax2;
      if (widthMinMax.getRange() > heightMinMax.getRange()) {
        fun1 = PatchData::getAvgLon;
        fun2 = PatchData::getAvgLat;
        minMax1 = widthMinMax;
        minMax2 = heightMinMax;
      } else {
        fun1 = PatchData::getAvgLat;
        fun2 = PatchData::getAvgLon;
        minMax1 = heightMinMax;
        minMax2 = widthMinMax;
      }

      List<Graph<PatchData, PatchConnectionData>> subParts = divide(graph, divisors.getLeft(), minMax1, fun1);

      return subParts.stream()
          .map(subGraph -> divide(subGraph, divisors.getRight(), minMax2, fun2))
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
      int partsCount, MinMaxAcc minMax, Function<PatchData, Optional<Double>> getter) {

    List<Graph<PatchData, PatchConnectionData>> dividedParts = new LinkedList<>();
    double partition_range = minMax.getRange() / partsCount;

    for (int i = 0; i < partsCount; i++) {
      final int finalI = i;
      List<Node<PatchData, PatchConnectionData>> graphNodes = graph.getNodes()
          .values()
          .stream()
          .filter(patchNode -> (getter.apply(patchNode.getData()).get() >= minMax.getMin() + partition_range * finalI
              && getter.apply(patchNode.getData()).get() < minMax.getMin() + partition_range * (finalI + 1)))
          .toList();

      GraphBuilder<PatchData, PatchConnectionData> graphBuilder = new GraphBuilder<>();
      graphNodes.forEach(graphBuilder::addNode);
      graphNodes.forEach(patchNode -> patchNode.getIncomingEdges().forEach(graphBuilder::addEdge));
      dividedParts.add(graphBuilder.build());
    }

    return dividedParts;
  }

  private Pair<Integer, Integer> calc_dims_divisors(int parts) {
    int x = (int) sqrt(parts);

    int diff = 1;
    while (parts % x != 0 && x >= 1) { // finds number near sqrt which divides parts without rest
      x += diff;

      if (diff < 0) {
        diff -= 1;
      } else {
        diff += 1;
      }
      diff *= -1;
    }
    if (x < 1) {
      x = 1;
    }

    return Pair.of(Math.max(x, parts / x), Math.min(x, parts / x));
  }
}
