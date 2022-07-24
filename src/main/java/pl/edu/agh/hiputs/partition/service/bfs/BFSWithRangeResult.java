package pl.edu.agh.hiputs.partition.service.bfs;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;

@Builder
@Getter
@AllArgsConstructor
public class BFSWithRangeResult<T extends NodeData, S extends EdgeData> {

  Set<Edge<T, S>> edgesInRange;

  Set<Node<T, S>> borderNodes;

}
