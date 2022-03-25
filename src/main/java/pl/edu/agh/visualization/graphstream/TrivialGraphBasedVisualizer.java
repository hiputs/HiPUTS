package pl.edu.agh.visualization.graphstream;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.car.CarRead;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.ILaneOnJunction;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.LaneRead;
import pl.edu.agh.model.map.Patch;

import java.util.ArrayList;

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
        for (Patch patch : mapFragment.getLocalPatches()) {
            for (Junction junction : patch.getJunctions().values()) {
                this.graph.addNode(junction.getId().getValue()).setAttribute("label", junction.getId().getValue().substring(0, 3));
            }
            for (Junction junction : patch.getJunctions().values()) {
                for (LaneId outgoinglaneId : junction.getOutgoingLanesIds()) {
                    LaneRead outgoingLane = patch.getLanes().get(outgoinglaneId);
                    this.graph.addEdge(outgoinglaneId.getValue(), junction.getId().getValue(), outgoingLane.getOutgoingJunction().getValue(), true);
                }
            }
        }
    }

    protected ArrayList<Sprite> spritesInPrevUpdate = new ArrayList<>();

    public void redrawCars() {
        ArrayList<Sprite> spritesInThisUpdate = new ArrayList<>();

        for (Patch patch : mapFragment.getLocalPatches()) {
            for (LaneRead lane : patch.getLanes().values()) {
                CarRead car = lane.getFirstCar().orElse(null);
                while (car != null) {
                    Sprite sprite = spriteManager.getSprite(car.getId().getValue());
                    if (sprite == null) {
                        sprite = spriteManager.addSprite(car.getId().getValue());
                        sprite.setAttribute("label", car.getId().getValue().substring(0, 3));
                        sprite.attachToEdge(lane.getId().getValue());
                    }
                    spritesInThisUpdate.add(sprite);
                    sprite.setPosition(car.getPosition() / lane.getLength());
                    int speedByte = (int) Math.min(car.getSpeed() * 10, 255);
                    sprite.setAttribute("ui.style", "fill-color: rgb(" + speedByte + "," + (255 - speedByte) + ",0);");

                    car = lane.getNextCarData(car).orElse(null);
                }
            }
        }

        spritesInPrevUpdate.removeAll(spritesInThisUpdate);
        spritesInPrevUpdate.forEach(sprite -> spriteManager.removeSprite(sprite.getId()));
        spritesInPrevUpdate = spritesInThisUpdate;
    }


    public void showGui() {
        this.graph.display();
    }


}
