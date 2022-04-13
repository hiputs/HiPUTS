package pl.edu.agh.hiputs.scheduler.task;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InjectIncomingCarsTask implements Runnable {

    private final List<SCar> sCars;
    private final TransferDataHandler transferDataHandler;

    @Override
    public void run() {
        transferDataHandler.acceptIncomingCars(
                sCars.parallelStream().map(SCar::toRealObject).collect(Collectors.toSet())
        );
    }
}
