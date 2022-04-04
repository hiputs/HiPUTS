package pl.edu.agh.hiputs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.simulation.MapFragmentExecutor;
import pl.edu.agh.hiputs.visualization.graphstream.TrivialGraphBasedVisualizer;

import javax.annotation.PostConstruct;

import static java.lang.Thread.sleep;

@Service
public class SimpleSimulationWithGUI implements Runnable {

    @Autowired
    public MapFragmentExecutor mapFragmentExecutor;

    TrivialGraphBasedVisualizer graphBasedVisualizer;

    @PostConstruct
    public void init() {
        graphBasedVisualizer = new TrivialGraphBasedVisualizer(mapFragmentExecutor.mapFragment);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startSimulation() {
        System.setProperty("java.awt.headless", "false");
        this.run();
    }

    @SneakyThrows
    @Override
    public void run() {
        graphBasedVisualizer.showGui();
        sleep(1000);

        while (true) {
            mapFragmentExecutor.run();
            graphBasedVisualizer.redrawCars();
            sleep(200);
        }
    }
}