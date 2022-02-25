package pl.edu.agh.visualization.graphstream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.model.actor.ActorContext;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.example.ExampleActorContextProvider;

import javax.xml.stream.FactoryConfigurationError;

import static org.junit.jupiter.api.Assertions.*;

class TrivialGraphBasedVisualizerTest {

    protected TrivialGraphBasedVisualizer trivialGraphBasedVisualizer = null;


    @BeforeEach
    void setUp() {
        trivialGraphBasedVisualizer = new TrivialGraphBasedVisualizer(ExampleActorContextProvider.getSimpleMap2());
    }

    @Test
    void showGui() {
        trivialGraphBasedVisualizer.showGui();

        try {

            for (int i = 0 ; i < 100 ; i ++)
            {
                Thread.sleep(100);
                //          s.setPosition((float)i/100);
            }
        } catch (InterruptedException e) {
        }


    }
}