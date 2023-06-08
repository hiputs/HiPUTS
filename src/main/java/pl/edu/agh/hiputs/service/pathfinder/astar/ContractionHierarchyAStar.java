/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package pl.edu.agh.hiputs.service.pathfinder.astar;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.util.ConcurrencyUtil;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * Implementation of the hierarchical query algorithm based on the bidirectional Dijkstra search.
 * This algorithm is designed to contracted graphs. The best speedup is achieved on sparse graphs
 * with low average outdegree.
 *
 * <p>
 * The query algorithm is originally described the article: Robert Geisberger, Peter Sanders,
 * Dominik Schultes, and Daniel Delling. 2008. Contraction hierarchies: faster and simpler
 * hierarchical routing in road networks. In Proceedings of the 7th international conference on
 * Experimental algorithms (WEA'08), Catherine C. McGeoch (Ed.). Springer-Verlag, Berlin,
 * Heidelberg, 319-333.
 *
 * <p>
 * During contraction graph is divided into 2 parts which are called upward and downward graphs.
 * Both parts have all vertices of the original graph. The upward graph ($G_{&#92;uparrow}$)
 * contains only those edges which source has lower level than the target and vice versa for the
 * downward graph ($G_{\downarrow}$).
 *
 * <p>
 * For the shortest path query from $s$ to $t$, a modified bidirectional Dijkstra shortest path
 * search is performed. The forward search from $s$ operates on $G_{&#92;uparrow}$ and the backward
 * search from $t$ - on the $G_{\downarrow}$. In each direction only the edges of the corresponding
 * part of the graph are considered. Both searches eventually meet at the vertex $v$, which has the
 * highest level in the shortest path from $s$ to $t$. Whenever a search in one direction reaches a
 * vertex that has already been processed in other direction, a new candidate for a shortest path is
 * found. Search is aborted in one direction if the smallest element in the corresponding priority
 * queue is at least as large as the best candidate path found so far.
 *
 * <p>
 * After computing a contracted path, the algorithm unpacks it recursively into the actual shortest
 * path using the bypassed edges stored in the contraction hierarchy graph.
 *
 * <p>
 * There is a possibility to provide an already computed contraction for the graph. For now there is
 * no means to ensure that the specified contraction is correct, nor to fail-fast. If algorithm uses
 * an incorrect contraction, the results of the search are unpredictable.
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see AStarContractionHierarchyPrecomputation
 * @since July 2019
 */
public class ContractionHierarchyAStar<V, E>
        extends
        BaseShortestPathAlgorithm<V, E>
{

    /**
     * Contraction hierarchy which is used to compute shortest paths.
     */
    public AStarContractionHierarchyPrecomputation.ContractionHierarchy<V, E> contractionHierarchy;
    /**
     * Contracted graph, which is used during the queries.
     */
    private final Graph<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> contractionGraph;
    /**
     * Mapping from original to contracted vertices.
     */
    private final Map<V, AStarContractionHierarchyPrecomputation.ContractionVertex<V>> contractionMapping;

    /**
     * Supplier for preferable heap implementation.
     */
    private final Supplier<
            AddressableHeap<Double, AStarContractionHierarchyPrecomputation.ContractionVertex<V>>> heapSupplier;

    /**
     * Radius of the search.
     */
    private final double radius;

    /**
     * Constructs a new instance of the algorithm for a given {@code graph} and {@code executor}. It
     * is up to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. For utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link ConcurrencyUtil}.
     *
     * @param graph the graph
     * @param executor executor which is used for computing the {@link AStarContractionHierarchyPrecomputation.ContractionHierarchy}
     */
    public ContractionHierarchyAStar(Graph<V, E> graph, ThreadPoolExecutor executor)
    {
        this(
                new AStarContractionHierarchyPrecomputation<>(graph, executor)
                        .computeContractionHierarchy());
    }

    /**
     * Constructs a new instance of the algorithm for a given {@code hierarchy}.
     *
     * @param hierarchy contraction of the {@code graph}
     */
    public ContractionHierarchyAStar(AStarContractionHierarchyPrecomputation.ContractionHierarchy<V, E> hierarchy)
    {
        this(hierarchy, Double.POSITIVE_INFINITY, PairingHeap::new);
    }

    /**
     * Constructs a new instance of the algorithm for the given {@code hierarchy}, {@code radius}
     * and {@code heapSupplier}.
     *
     * @param hierarchy contraction of the {@code graph}
     * @param radius search radius
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public ContractionHierarchyAStar(
            AStarContractionHierarchyPrecomputation.ContractionHierarchy<V, E> hierarchy, double radius, Supplier<
            AddressableHeap<Double, AStarContractionHierarchyPrecomputation.ContractionVertex<V>>> heapSupplier)
    {
        super(hierarchy.getGraph());
        this.contractionHierarchy = hierarchy;
        this.contractionGraph = hierarchy.getContractionGraph();
        this.contractionMapping = hierarchy.getContractionMapping();
        this.radius = radius;
        this.heapSupplier = heapSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }

        // handle special case if source equals target
        if (source.equals(sink)) {
            return createEmptyPath(source, sink);
        }

        AStarContractionHierarchyPrecomputation.ContractionVertex<V> contractedSource = contractionMapping.get(source);
        AStarContractionHierarchyPrecomputation.ContractionVertex<V> contractedSink = contractionMapping.get(sink);

        AStarHeuristicForJunction heuristic = new AStarHeuristicForJunction();

        // create frontiers
        AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> forwardFrontier =
                new AStarContractionSearchFrontier(
                        new MaskSubgraph<>(contractionGraph, v -> false, e -> !e.isUpward), sink, heapSupplier, heuristic);

        AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>,
                AStarContractionHierarchyPrecomputation.ContractionEdge<E>> backwardFrontier = new AStarContractionSearchFrontier(
                new MaskSubgraph<>(
                        new EdgeReversedGraph<>(contractionGraph), v -> false, e -> e.isUpward), source,
                heapSupplier, heuristic);

        // initialize both frontiers
        forwardFrontier.updateDistance(contractedSource, null, 0d, 0d);
        backwardFrontier.updateDistance(contractedSink, null, 0d, 0d);

        // initialize best path
        double bestPath = Double.POSITIVE_INFINITY;
        AStarContractionHierarchyPrecomputation.ContractionVertex<V> bestPathCommonVertex = null;

        AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> frontier =
                forwardFrontier;
        AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> otherFrontier =
                backwardFrontier;

        while (true) {
            if (frontier.openList.isEmpty()) {
                frontier.isFinished = true;
            }
            //System.out.println("In loop");
            if (otherFrontier.openList.isEmpty()) {
                otherFrontier.isFinished = true;
            }

            // stopping condition for search
            if (frontier.isFinished ) {//&& otherFrontier.isFinished) {
                //System.out.println("Breaking");
                break;
            }

            // stopping condition for current frontier
            if (frontier.openList.findMin().getKey() >= bestPath) {
                //System.out.println("Min path");
                frontier.isFinished = true;
            } else {

                // frontier scan
                AddressableHeap.Handle<Double,
                        AStarContractionHierarchyPrecomputation.ContractionVertex<V>> node =
                        frontier.openList.deleteMin();
                AStarContractionHierarchyPrecomputation.ContractionVertex<V> v = node.getValue();
                //double vDistance = node.getKey();
                double vDistance = frontier.gScoreMap.get(v);
                //System.out.println("Distance: " + vDistance + " Node: " + node.getValue().vertex);

                for (AStarContractionHierarchyPrecomputation.ContractionEdge<E> e : frontier.graph.outgoingEdgesOf(v)) {
                    AStarContractionHierarchyPrecomputation.ContractionVertex<V> u = frontier.graph.getEdgeTarget(e);

                    double eWeight = frontier.graph.getEdgeWeight(e);
                    double fScore = heuristic.getCostEstimate((JunctionReadable) u.vertex, (JunctionReadable)sink) + vDistance + eWeight;
                    //System.out.println("Update: " + v.vertex + ", edge: " + e.edge + ", ten: " + (vDistance + eWeight) + ", Score: " + fScore);
                    frontier.updateDistance(u, e, vDistance + eWeight, fScore);

                    // check path with u's distance from the other frontier
                    //System.out.println("U vertex:" + u.vertex + " - " + contractedSink.vertex.toString());
                    //System.out.println("Other distance: " + otherFrontier.getDistance(u));
                    double pathDistance = vDistance + eWeight + otherFrontier.getDistance(u);

                    if (pathDistance < bestPath) {
                        bestPath = pathDistance;
                        //System.out.println("AStar Path distance:" + bestPath);
                        bestPathCommonVertex = u;
                    }
                }
            }

            // swap frontiers only if the other frontier is not yet finished
            if (!otherFrontier.isFinished) {
                AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>,
                        AStarContractionHierarchyPrecomputation.ContractionEdge<E>> tmpFrontier =
                        frontier;
                frontier = otherFrontier;
                otherFrontier = tmpFrontier;
            }
        }

        // create path if found
        if (Double.isFinite(bestPath) && bestPath <= radius) {
            assert bestPathCommonVertex != null;
            return createPath(
                    forwardFrontier, backwardFrontier, bestPath, contractedSource, bestPathCommonVertex,
                    contractedSink);
        } else {
            return createEmptyPath(source, sink);
        }
    }

    /**
     * Builds shortest unpacked path between {@code source} and {@code sink} based on the
     * information provided by search frontiers and common vertex.
     *
     * @param forwardFrontier forward direction frontier
     * @param weight weight of the shortest path
     * @param source path source
     * @param commonVertex path common vertex
     * @param sink path sink
     * @return unpacked shortest path between source and sink
     */
    private GraphPath<V, E> createPath(
            AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> forwardFrontier,
            AStarContractionSearchFrontier<AStarContractionHierarchyPrecomputation.ContractionVertex<V>, AStarContractionHierarchyPrecomputation.ContractionEdge<E>> backwardFrontier,
            double weight, AStarContractionHierarchyPrecomputation.ContractionVertex<V> source, AStarContractionHierarchyPrecomputation.ContractionVertex<V> commonVertex,
            AStarContractionHierarchyPrecomputation.ContractionVertex<V> sink)
    {
        LinkedList<E> edgeList = new LinkedList<>();
        LinkedList<V> vertexList = new LinkedList<>();

        // add common vertex
        vertexList.add(commonVertex.vertex);

        // traverse forward path
        AStarContractionHierarchyPrecomputation.ContractionVertex<V> v = commonVertex;
        while (true) {
            AStarContractionHierarchyPrecomputation.ContractionEdge<E> e = forwardFrontier.getTreeEdge(v);
            if (e == null) {
                break;
            }
            contractionHierarchy.unpackBackward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeSource(e);
        }

        // traverse reverse path
        v = commonVertex;
        while (true) {
            AStarContractionHierarchyPrecomputation.ContractionEdge<E> e = backwardFrontier.getTreeEdge(v);

            if (e == null) {
                break;
            }

            contractionHierarchy.unpackForward(e, vertexList, edgeList);
            v = contractionGraph.getEdgeTarget(e);
        }
        return new GraphWalk<>(graph, source.vertex, sink.vertex, vertexList, edgeList, weight);
    }

    /**
     * Maintains search frontier during shortest path computation.
     *
     * @param <V> vertices type
     * @param <E> edges type
     */
    static class AStarContractionSearchFrontier<V, E>
            extends
            AStarSearchFrontier<V, E>
    {
        boolean isFinished;

        /**
         * Constructs an instance of a search frontier for the given graph, heap supplier and
         * {@code isDownwardEdge} function.
         *
         * @param graph the graph
         * @param heapSupplier supplier for the preferable heap implementation
         */
        AStarContractionSearchFrontier(
                Graph<V, E> graph,
                V sink,
                Supplier<AddressableHeap<Double, V>> heapSupplier,
                AStarAdmissibleHeuristic<V> heuristic)
        {
            super(graph, sink, heapSupplier, heuristic);
        }
    }

    static class AStarSearchFrontier<V, E>
            extends BaseBidirectionalShortestPathAlgorithm.BaseSearchFrontier<V, E>
    {
        /**
         * End vertex of the frontier.
         */
        final V endVertex;
        /**
         * Heuristic used in this frontier.
         */
        final AStarAdmissibleHeuristic<V> heuristic;
        /**
         * Open nodes of the frontier.
         */
        final AddressableHeap<Double, V> openList;
        final Map<V, AddressableHeap.Handle<Double, V>> vertexToHeapNodeMap;
        /**
         * Closed nodes of the frontier.
         */
        final Set<V> closedList;

        /**
         * Tentative distance to the vertices in tha graph computed so far.
         */
        final Map<V, Double> gScoreMap;
        /**
         * Predecessor map.
         */
        final Map<V, E> cameFrom;

        AStarSearchFrontier(Graph<V, E> graph,
                            V endVertex,
                            Supplier<AddressableHeap<Double, V>> heapSupplier,
                            AStarAdmissibleHeuristic<V> heuristic
        )
        {
            super(graph);
            this.endVertex = endVertex;
            this.heuristic = heuristic;
            openList = heapSupplier.get();
            vertexToHeapNodeMap = new HashMap<>();
            closedList = new HashSet<>();
            gScoreMap = new HashMap<>();
            cameFrom = new HashMap<>();
        }

        void updateDistance(V v, E e, double tentativeGScore, double fScore)
        {
            AddressableHeap.Handle<Double, V> node = vertexToHeapNodeMap.get(v);
            if (vertexToHeapNodeMap.containsKey(v)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(v)) {// Ignore path since it is non-improving
                    return;
                }

                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);

                if (closedList.contains(v)) { // it's in the closed list. Move node back to
                    // open list, since we discovered a shorter path to this node
                    closedList.remove(v);
                    openList.insert(fScore, v);
                } else if (node.getKey() > fScore) { // It's in the open list
                    //System.out.println(node.getKey() + "== " + fScore + " ! " + tentativeGScore);

                    node.decreaseKey(fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(v, e);
                gScoreMap.put(v, tentativeGScore);
                node = openList.insert(fScore, v);
                vertexToHeapNodeMap.put(v, node);
            }
        }

        @Override
        double getDistance(V v)
        {
            Double distance = gScoreMap.get(v);
            return Objects.requireNonNullElse(distance, Double.POSITIVE_INFINITY);
        }

        @Override
        E getTreeEdge(V v)
        {
            return cameFrom.get(v);
        }
    }
}