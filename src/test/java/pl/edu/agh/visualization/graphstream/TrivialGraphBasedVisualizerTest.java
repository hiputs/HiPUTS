package pl.edu.agh.visualization.graphstream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.model.actor.ActorContext;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.example.ExampleActorContextProvider;
import pl.edu.agh.model.map.example.ExampleCarProvider;

import javax.xml.stream.FactoryConfigurationError;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TrivialGraphBasedVisualizerTest {
    @Test
    void showGui() {

        ActorContext ac = ExampleActorContextProvider.getSimpleMap2();
        TrivialGraphBasedVisualizer trivialGraphBasedVisualizer = new TrivialGraphBasedVisualizer(ac);

        trivialGraphBasedVisualizer.showGui();

        for (int i = 0 ; i < 10 ; i ++)
        {
            try { Thread.sleep(1000);} catch (InterruptedException e) {}
            trivialGraphBasedVisualizer.updateCarsState();
        }



    }
}