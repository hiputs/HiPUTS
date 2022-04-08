package pl.edu.agh.visualization.graphstream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.SpriteManager;

public class OsmPartitionerVisualizer {

    private final String graphStyles = """
            node { size: 7px; fill-color: #777; text-mode: hidden; z-index: 0; }
            edge { shape: line; fill-color: #222; arrow-size: 6px, 5px; }
            edge.knownMaxSpeed { shape: line; fill-color: #600; arrow-size: 6px, 5px; }
            sprite { text-color: rgb(255,255,255); size: 18; text-size: 15;  }
            """;

    protected Graph graph;
    protected SpriteManager spriteManager;

    protected pl.edu.agh.partition.model.Graph map;


    public OsmPartitionerVisualizer(pl.edu.agh.partition.model.Graph map) {
        this.map = map;

        this.graph = new SingleGraph("The city");
        this.graph.setAttribute("ui.stylesheet", graphStyles);
        System.setProperty("org.graphstream.ui", "swing");
        spriteManager = new SpriteManager(this.graph);

        buildGraphStructure();
    }

    protected void buildGraphStructure() {
        for (pl.edu.agh.partition.model.Node osmNode : map.getNodes().values()) {
            Node node = this.graph.addNode(osmNode.getId());
            node.setAttribute("label", osmNode.getId().substring(0, 3));
            node.setAttribute("xy", osmNode.getLon(), osmNode.getLat());
        }

        for (pl.edu.agh.partition.model.Edge osmEdge : map.getEdges().values()) {
            Edge edge = this.graph.addEdge(osmEdge.getId(), osmEdge.getSource().getId(), osmEdge.getTarget().getId(), true);
            if(osmEdge.getMaxSpeed() > 0 ) {
                edge.setAttribute("ui.class", "knownMaxSpeed");
            }
        }
    }

    public void showGui() {
        this.graph.display(false);
    }

}
