package pl.edu.agh.hiputs.visualization.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.utils.CoordinatesUtil;

import java.io.IOException;
import java.util.List;
@Slf4j
@Component
public class CarsProducer {

    public String getCars(MapFragment mapFragment, int iterationNumber, MapRepository mapRepository) throws IOException {
        return new ObjectMapper().writeValueAsString(new Message("cars",createCarsMessagesList(mapFragment,mapRepository)));
    }

    private static List<CarMessage> createCarsMessagesList(MapFragment mapFragment,MapRepository mapRepository) {
        return mapFragment.getKnownPatchReadable().stream()
                .flatMap(PatchReader::streamLanesReadable)
                .flatMap(LaneReadable::streamCarsFromExitReadable)
                .map(carReadable -> createCarMessage(carReadable, mapFragment,mapRepository))
                .toList();
    }

    private static CarMessage createCarMessage(CarReadable car, MapFragment mapFragment,MapRepository mapRepository) {
        car.getLaneId();
        Lane carLane = (Lane) mapFragment.getLaneReadable(car.getLaneId());
        PatchId patchIdByLaneId = mapFragment.getPatchIdByLaneId(car.getLaneId());
        Coordinates start = carLane.getStart();
        Coordinates end = carLane.getEnd();


        double positionOnLane = car.getPositionOnLane() / carLane.getLength();

        Coordinates position = CoordinatesUtil.getCoordinatesFromTwoPointAndDistance(start,end,positionOnLane);

        //TODO replace with builder
        CarMessage carMessage = new CarMessage();
        carMessage.setCarId(car.getCarId().getValue());
        carMessage.setLength(car.getLength());
        carMessage.setAcceleration(car.getAcceleration());
        carMessage.setSpeed(car.getSpeed());
        carMessage.setMaxSpeed(car.getMaxSpeed());
        carMessage.setNode1Id(carLane.getIncomingJunctionId().getValue());
        carMessage.setNode2Id(carLane.getOutgoingJunctionId().getValue());
        carMessage.setPositionOnLane(positionOnLane);
        carMessage.setPosition(position.getCoordinates());
        return carMessage;
    }
}

