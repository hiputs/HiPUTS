package pl.edu.agh.hiputs.visualization.graphstream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

/**
 * Simple visualization
 * <p> based on https://graphstream-project.org/doc/Tutorials/Graph-Visualisation/ </p>
 * <p> Usage: construct, call showGui() and call updateCarsState when needed. </p>
 * <p> Does not support changes in patches structure ! </p>
 **/

public class TrivialGraphBasedVisualizer {

  private final String graphStyles = """          
      node.crossroad { fill-color: rgb(24,116,152); text-color: rgb(255,255,255); size: 25; text-size: 15; }
      node.bend { fill-color: rgb(24,116,152); text-color: rgb(255,255,255); size: 10; text-size: 15; }
      edge.local {  fill-color: rgb(54, 174, 124); text-size: 15; }
      edge.border {  fill-color: rgb(249, 217, 35); text-size: 15; }
      edge.remote {  fill-color: rgb(235, 83, 83); text-size: 15; }
      sprite { text-color: rgb(255,255,255); size: 18; text-size: 15;  }
      """;

  private final String vehicleSpriteTemplate = "fill-color: rgb(%d,%d,0); shape: box; size: 15, 10;";

  protected Graph graph;
  protected SpriteManager spriteManager;

  protected MapFragment mapFragment;

  protected boolean drawBasedOnCoordinates;

  public TrivialGraphBasedVisualizer(MapFragment mapFragment) {
    this.mapFragment = mapFragment;

    this.graph = new SingleGraph("The city");
    this.graph.setStrict(false);
    this.graph.setAutoCreate(true);
    this.graph.setAttribute("ui.stylesheet", graphStyles);
    System.setProperty("org.graphstream.ui", "swing");
    spriteManager = new SpriteManager(this.graph);

    buildGraphStructure();
  }

  protected void buildGraphStructure() {
    drawBasedOnCoordinates = mapFragment.getLocalJunctionIds().stream().map(mapFragment::getJunctionReadable)
        .noneMatch(junctionReadable -> junctionReadable.getLongitude() == null || junctionReadable.getLatitude() == null);

    mapFragment.getLocalJunctionIds().forEach(this::drawJunction);

    mapFragment.getLocalLaneIds().stream()
        .map(laneId -> laneId.getReadable(mapFragment))
        .forEach(laneReadable -> drawLane(laneReadable, getLaneUiClass(laneReadable.getLaneId())));

    mapFragment.getShadowPatchesReadable().stream()
        .map(PatchReader::getLaneIds)
        .flatMap(Collection::stream)
        .map(laneId -> laneId.getReadable(mapFragment))
        .forEach(laneReadable -> drawLane(laneReadable, "remote"));
  }

  private void drawLane(LaneReadable laneReadable, String laneType) {
    LaneId laneId = laneReadable.getLaneId();
    JunctionId junctionId1 = laneReadable.getIncomingJunctionId();
    JunctionId junctionId2 = laneReadable.getOutgoingJunctionId();
    Edge edge = this.graph.addEdge(laneId.getValue(), junctionId1.getValue(), junctionId2.getValue(), true);
    edge.setAttribute("ui.class", laneType);
  }

  private void drawJunction(JunctionId junctionId) {
    Node node = this.graph.addNode(junctionId.getValue());
    node.setAttribute("label", junctionId.getValue().substring(0, 3));
    if (drawBasedOnCoordinates) {
      JunctionReadable junctionReadable = mapFragment.getJunctionReadable(junctionId);
      node.setAttribute("xy",
          CoordinatesUtil.longitude2plain(junctionReadable.getLongitude(), junctionReadable.getLatitude()),
          CoordinatesUtil.latitude2plain(junctionReadable.getLatitude()));
      node.setAttribute("ui.class", junctionId.isCrossroad() ? "crossroad" : "bend");
    }
  }

  private String getLaneUiClass(LaneId laneId) {
    Set<LaneId> borderLanes = mapFragment.getBorderPatches()
        .values()
        .stream()
        .flatMap(Collection::stream)
        .map(Patch::getLaneIds)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    if (borderLanes.contains(laneId)) return "border";
    return "local";
  }

  protected ArrayList<Sprite> spritesInPrevUpdate = new ArrayList<>();

  public void redrawCars() {
    ArrayList<Sprite> spritesInThisUpdate = new ArrayList<>();

    mapFragment.getKnownPatchReadable().stream().flatMap(PatchReader::streamLanesReadable).forEach(lane -> {
      CarReadable car = lane.getCarAtEntryReadable().orElse(null);
      while (car != null) {
        Sprite sprite = spriteManager.getSprite(car.getCarId().getValue() + lane.getLaneId().getValue());
        if (sprite == null) {
          sprite = spriteManager.addSprite(car.getCarId().getValue() + lane.getLaneId().getValue());
          sprite.setAttribute("label", car.getCarId().getValue().substring(0, 3));
          sprite.attachToEdge(lane.getLaneId().getValue());
        }
        spritesInThisUpdate.add(sprite);
        sprite.setPosition(car.getPositionOnLane() / lane.getLength());
        int speedByte = (int) Math.min(car.getSpeed() * 10, 255);
        sprite.setAttribute("ui.style", String.format(vehicleSpriteTemplate, speedByte, 255 - speedByte));

        car = lane.getCarInFrontReadable(car).orElse(null);
      }
    });

    spritesInPrevUpdate.removeAll(spritesInThisUpdate);
    spritesInPrevUpdate.forEach(sprite -> spriteManager.removeSprite(sprite.getId()));
    spritesInPrevUpdate = spritesInThisUpdate;
  }

  public void showGui() {
    this.graph.display(!drawBasedOnCoordinates);
  }

}
