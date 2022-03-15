package pl.edu.agh.scheduler.task;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.communication.model.serializable.SCar;
import pl.edu.agh.model.map.LaneReadWrite;
import pl.edu.agh.model.map.Patch;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CarMapperTask implements Runnable {

    /**
     * Patch to serialized
     */
    private final Patch patch;

    /**
     * List to save collection after serialized
     */
    private final LinkedList<SCar> serializedCar;

    @Override
    public void run() {
        serializedCar.addAll(patch.getLanes()
                .values()
                .stream()
                .map(LaneReadWrite::getIncomingCars)
                .map(cars -> cars
                        .stream()
                        .map(SCar::new)
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .toList());
    }
}
