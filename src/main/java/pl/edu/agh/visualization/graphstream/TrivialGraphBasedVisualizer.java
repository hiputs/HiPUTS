package pl.edu.agh.visualization.graphstream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import pl.edu.agh.model.actor.ActorContext;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.LaneReadOnly;
import pl.edu.agh.model.map.Patch;


//https://graphstream-project.org/doc/Tutorials/Graph-Visualisation/

// does not support changes in pathes structure !

public class TrivialGraphBasedVisualizer {

    private final String graphStyles = """
                node { fill-color: rgb(0,50,200); text-color: rgb(255,255,255); size: 25; text-size: 15; }
                edge {  fill-color: rgb(150,150,150); text-size: 15;}
                sprite { text-color: rgb(255,255,255); size: 20; text-size: 15;  }                
                """;

    protected Graph graph;
    protected SpriteManager spriteManager;
    protected ActorContext actorContext;


    public TrivialGraphBasedVisualizer(ActorContext actorContext)
    {
        this.actorContext = actorContext;

        this.graph = new SingleGraph("The city");
        this.graph.setStrict(false);
        this.graph.setAutoCreate(true);
        this.graph.setAttribute("ui.stylesheet", graphStyles);
        System.setProperty("org.graphstream.ui", "swing");
        spriteManager = new SpriteManager(this.graph);

        buildGraphStructure();

    }

    protected void buildGraphStructure()
    {
        for (Patch patch : actorContext.getLocalPatches()) {
            for (Junction junction : patch.getJunctions().values())
            {
                this.graph.addNode(junction.getId().getValue()).setAttribute("label", junction.getId().getValue().substring(0,3));
            }
            for (Junction junction : patch.getJunctions().values())
            {
                for (LaneId outgoinglaneId : junction.getOutgoingLanes())
                {
                    LaneReadOnly outgoingLane = patch.getLanes().get(outgoinglaneId);
                    this.graph.addEdge(outgoinglaneId.getValue(), junction.getId().getValue(), outgoingLane.getOutgoingJunction().getValue(), true);
                }
            }
        }
    }



    public void showGui()  {


        this.graph.display();

//
//
//
//        this.graph.addNode("A" );
//        this.graph.addNode("B" );
//        this.graph.addNode("C" );
//        this.graph.addEdge("AB", "A", "B", true);
//        this.graph.addEdge("BC", "B", "C", true);
//        this.graph.addEdge("CA", "C", "A", true);
//        this.graph.addEdge("AC", "A", "C", true);
//
//        Edge AB = this.graph.getEdge("AB");
//        AB.setAttribute("label", "lane-3");
//        for (Node node : this.graph) {
//            node.setAttribute("label", "3");
//        }
//
//        SpriteManager sman = new SpriteManager(this.graph);
//        Sprite s = sman.addSprite("S1");
//
//        s.attachToEdge("AB");
//        s.setAttribute("label", "      car-1");
//        s.setAttribute("ui.style", "fill-color: rgb(0,100,0);");
//
//
//
//




    }


}
