// package pl.edu.agh.hiputs.partition.mapper.transformer;
//
// import java.util.Deque;
// import java.util.LinkedList;
// import java.util.Map;
// import java.util.Map.Entry;
// import java.util.Optional;
// import java.util.Set;
// import java.util.function.Function;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
// import org.apache.logging.log4j.util.Strings;
// import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Service;
// import pl.edu.agh.hiputs.partition.model.JunctionData;
// import pl.edu.agh.hiputs.partition.model.WayData;
// import pl.edu.agh.hiputs.partition.model.graph.Graph;
// import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
// import pl.edu.agh.hiputs.partition.model.graph.Node;
//
// @Service
// @Order(1)
// public class LargestCCSelector implements GraphTransformer {
//
//   private Map<String, String> visitationMap;
//
//   @Override
//   public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
//     initVisitationMap(graph);
//
//     Optional<String> root = getUnvisitedNodeId();
//     String largestConnectedComponentId = root.get();
//     int largestCCNodesCount = 0;
//     while(root.isPresent()) {
//       int currentCCNodesCount = dfsColoring(graph, root.get());
//       if (currentCCNodesCount > largestCCNodesCount) {
//         largestConnectedComponentId = root.get();
//         largestCCNodesCount = currentCCNodesCount;
//       }
//       root = getUnvisitedNodeId();
//     }
//
//     Graph.GraphBuilder<JunctionData, WayData> graphBuilder = new GraphBuilder<>();
//
//     final String finalLargestConnectedComponentId = largestConnectedComponentId;
//     Set<Node<JunctionData, WayData>> nodes = visitationMap.entrySet()
//         .stream()
//         .filter(e -> e.getValue().equals(finalLargestConnectedComponentId))
//         .map(Entry::getKey)
//         .map(nodeId -> graph.getNodes().get(nodeId))
//         .collect(Collectors.toSet());
//
//     nodes.forEach(graphBuilder::addNode);
//
//     nodes.forEach(n -> {
//       Stream.concat(n.getIncomingEdges().stream(), n.getOutgoingEdges().stream())
//           .forEach(graphBuilder::addEdge);
//     });
//
//     return graphBuilder.build();
//   }
//
//   private int dfsColoring(Graph<JunctionData, WayData> graph, String rootNodeId) {
//     Deque<Node<JunctionData, WayData>> deque = new LinkedList<>();
//     deque.push(graph.getNodes().get(rootNodeId));
//     int coloredNodesCount = 0;
//     while(!deque.isEmpty()) {
//       Node<JunctionData, WayData> currentNode = deque.pop();
//       visitationMap.put(currentNode.getId(), rootNodeId);
//       coloredNodesCount++;
//
//       Stream.concat(currentNode.getIncomingEdges().stream(), currentNode.getOutgoingEdges().stream())
//           .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
//           .collect(Collectors.toSet())
//           .forEach(n -> {
//             if (Strings.isBlank(visitationMap.get(n.getId()))) {
//               deque.push(n);
//             }
//           });
//     }
//     return coloredNodesCount;
//   }
//
//   private void initVisitationMap(Graph<JunctionData, WayData> graph) {
//     visitationMap = graph.getNodes()
//         .keySet()
//         .stream()
//         .collect(Collectors.toMap(Function.identity(), x -> ""));
//   }
//
//   private Optional<String> getUnvisitedNodeId() {
//     return visitationMap.entrySet().stream().filter(e -> Strings.isBlank(e.getValue())).findAny().map(Entry::getKey);
//   }
//
// }
