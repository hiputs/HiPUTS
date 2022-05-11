package pl.edu.agh.hiputs.visualization.graphstream;

import java.util.ArrayList;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.utils.CoordinatesUtil;

/**
 * Simple visualization
 * <p> based on https://graphstream-project.org/doc/Tutorials/Graph-Visualisation/ </p>
 * <p> Usage: construct, call showGui() and call updateCarsState when needed. </p>
 * <p> Does not support changes in patches structure ! </p>
 **/

public class TrivialGraphBasedVisualizer {

  private final String graphStyles = """
                  
      node { fill-color: rgb(0,50,200); text-color: rgb(255,255,255); shape: box; size: 25; text-size: 15; }
      edge {  fill-color: rgb(150,150,150); text-size: 15;}
      sprite { text-color: rgb(255,255,255); size: 18; text-size: 15;  }
      """;

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

    mapFragment.getLocalJunctionIds()
        .forEach(junctionId -> {
          Node node = this.graph.addNode(junctionId.getValue());
          node.setAttribute("label", junctionId.getValue().substring(0, 3));
          if (drawBasedOnCoordinates) {
            JunctionReadable junctionReadable = mapFragment.getJunctionReadable(junctionId);
            node.setAttribute("xy",
                CoordinatesUtil.longitude2plain(junctionReadable.getLongitude(), junctionReadable.getLatitude()),
                CoordinatesUtil.latitude2plain(junctionReadable.getLatitude()));
          }
        });
    mapFragment.getLocalLaneIds().stream().map(laneId -> laneId.getReadable(mapFragment)).forEach(laneReadable -> {
      LaneId laneId = laneReadable.getLaneId();
      JunctionId junctionId1 = laneReadable.getIncomingJunctionId();
      JunctionId junctionId2 = laneReadable.getOutgoingJunctionId();
      this.graph.addEdge(laneId.getValue(), junctionId1.getValue(), junctionId2.getValue(), true);
    });
  }

  protected ArrayList<Sprite> spritesInPrevUpdate = new ArrayList<>();

  public void redrawCars() {
    ArrayList<Sprite> spritesInThisUpdate = new ArrayList<>();

    mapFragment.getLocalLaneIds().stream().map(mapFragment::getLaneReadable).forEach(lane -> {
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
        sprite.setAttribute("ui.style", "fill-color: rgb(" + speedByte + "," + (255 - speedByte) + ",0);");

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
