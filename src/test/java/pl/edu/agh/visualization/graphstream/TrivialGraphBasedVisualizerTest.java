package pl.edu.agh.visualization.graphstream;

import org.junit.jupiter.api.Test;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.map.example.ExampleMapFragmentProvider;

class TrivialGraphBasedVisualizerTest {
    @Test
    void showGui() {

        MapFragment mp = ExampleMapFragmentProvider.getSimpleMap2();
        TrivialGraphBasedVisualizer trivialGraphBasedVisualizer = new TrivialGraphBasedVisualizer(mp);

        trivialGraphBasedVisualizer.showGui();

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            trivialGraphBasedVisualizer.redrawCars();
        }


    }
}