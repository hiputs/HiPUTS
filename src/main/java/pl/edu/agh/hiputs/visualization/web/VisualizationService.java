package pl.edu.agh.hiputs.visualization.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;


@Slf4j
@Service
@RequiredArgsConstructor
public class VisualizationService {

    @Autowired
    private WebSocketHandler webSocketHandler;
    private final CarsProducer carsProducer;
    private final MapProducer mapProducer;

    private final MapRepository mapRepository;

    @PostConstruct
    public void init(){
        webSocketHandler.subcribe(this);
    }

    public void sendCars(MapFragment mapFragment, int iterationNumber) throws IOException {
        webSocketHandler.sendMessage(carsProducer.getCars(mapFragment,iterationNumber,mapRepository));
    }


    public void sendCoordinates(){

        try {
            mapRepository.readMapAndBuildModel();
            Storage.setPatches(mapRepository.getAllPatches());
            String mapCoordinates = mapProducer.getMapCoordinates(Storage.getPatches());
            webSocketHandler.sendMessage(mapCoordinates);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    public void sendCarsFromMapFragment(MapFragment mapFragment, int iterationNumber) {
//        this.carsProducer.sendCars(mapFragment, iterationNumber);
//    }
//
//    public void sendSimulationStateChangeMessage(RUNNING_STATE runningState) {
//        simulationStateChangeProducer.sendStateChangeMessage(runningState);
//    }
//
//    public void sendNewNodes(List<Patch> patches) {
//        simulationNewNodesProducer.sendSimulationNotOsmNodesTransferMessage(patches);
//    }
//
//    public List<Node> getNodes(){
//        try {
//            mapRepository.readMapAndBuildModel();
//            Storage.setPatches(mapRepository.getAllPatches());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        return SimulationNewNodesProducer.getNotOsmNodesList(Storage.getPatches());
//    }
}
