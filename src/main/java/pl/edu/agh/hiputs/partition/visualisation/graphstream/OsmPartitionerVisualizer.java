package pl.edu.agh.hiputs.partition.visualisation.graphstream;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

@Slf4j
public class OsmPartitionerVisualizer {

  private final StringBuilder graphStylesBuilder = new StringBuilder("""
      node { size: 3px; fill-color: #777; text-mode: hidden; z-index: 0; }
      edge { shape: line; size: 3px; fill-color: #222; arrow-size: 1px, 1px; }
      sprite { text-color: rgb(255,255,255); size: 18; text-size: 15;  }
      """);

  protected Graph graph;
  protected SpriteManager spriteManager;

  protected pl.edu.agh.hiputs.partition.model.graph.Graph<JunctionData, WayData> map;

  public OsmPartitionerVisualizer(
      pl.edu.agh.hiputs.partition.model.graph.Graph<JunctionData, WayData> map) {
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
      node.setAttribute("ui.label", osmNode.getId().length() > 3 ? osmNode.getId().substring(0, 3) : osmNode.getId());
      node.setAttribute("xy", CoordinatesUtil.longitude2plain(osmNode.getData().getLon(), osmNode.getData().getLat()),
          CoordinatesUtil.latitude2plain(osmNode.getData().getLat()));
    }

    for (val osmEdge : map.getEdges().values()) {
      Edge edge = this.graph.addEdge(osmEdge.getId(), osmEdge.getSource().getId(), osmEdge.getTarget().getId(), true);
      edge.setAttribute("ui.class", "e" +  osmEdge.getData().getPatchId().replace("-", ""));
      graphStylesBuilder.append(generateStyleFromUUID(edge.getId(), osmEdge.getData().getPatchId()));
    }
  }

  public void showGui() {
    this.graph.display(false);
  }

  private String generateStyleFromUUID(String id, String patchId) {
    return "edge.e" +  patchId.replace("-", "") + " { shape: line; size: 3px; fill-color: #" + int2rgb(patchId.hashCode())
        + "; arrow-size: 0px, 0px; }\n";
  }

  private String int2rgb(int i) {
    String s = Integer.toHexString(Math.abs(i) % 4096);
    if (s.length() == 1) return "00" + s;
    if (s.length() == 2) return "0" + s;
    return s;
  }
}
