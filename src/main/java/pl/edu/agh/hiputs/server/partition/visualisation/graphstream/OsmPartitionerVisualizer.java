package pl.edu.agh.hiputs.server.partition.visualisation.graphstream;

import lombok.val;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.WayData;

public class OsmPartitionerVisualizer {

  private final StringBuilder graphStylesBuilder = new StringBuilder("""
      node { size: 7px; fill-color: #777; text-mode: hidden; z-index: 0; }
      edge { shape: line; fill-color: #222; arrow-size: 0px, 0px; }
      sprite { text-color: rgb(255,255,255); size: 18; text-size: 15;  }
      """);

  protected Graph graph;
  protected SpriteManager spriteManager;

  protected pl.edu.agh.hiputs.server.partition.model.graph.Graph<JunctionData, WayData> map;

  public OsmPartitionerVisualizer(
      pl.edu.agh.hiputs.server.partition.model.graph.Graph<JunctionData, WayData> map) {
    this.map = map;

    this.graph = new SingleGraph("The city");
    // this.graph.setAttribute("ui.stylesheet", graphStyles);
    System.setProperty("org.graphstream.ui", "swing");
    spriteManager = new SpriteManager(this.graph);

    buildGraphStructure();
    this.graph.setAttribute("ui.stylesheet", graphStylesBuilder.toString());
  }

  protected void buildGraphStructure() {
    for (val osmNode : map.getNodes().values()) {
      Node node = this.graph.addNode(osmNode.getId());
      node.setAttribute("label", osmNode.getId().substring(0, 3));
      node.setAttribute("xy", longitude2plain(osmNode.getData().getLon(), osmNode.getData().getLat()),
          latitude2plain(osmNode.getData().getLat()));
    }

    for (val osmEdge : map.getEdges().values()) {
      Edge edge = this.graph.addEdge(osmEdge.getId(), osmEdge.getSource().getId(), osmEdge.getTarget().getId(), true);
      edge.setAttribute("ui.class", edge.getId());
      graphStylesBuilder.append(generateStyleFromUUID(edge.getId()));
    }
  }

  public void showGui() {
    this.graph.display(false);
  }

  private Double latitude2plain(Double lat) {
    return 6371 * Math.toRadians(lat);
  }

  private Double longitude2plain(Double lon, Double lat) {
    return 6371 * Math.toRadians(lon) * Math.cos(Math.toRadians(lat));
  }

  private String generateStyleFromUUID(String id) {
    return id + " { shape: line; fill-color: #" + Integer.toHexString(id.hashCode() % 4096)
        + "; arrow-size: 0px, 0px; }\n";
  }
}
