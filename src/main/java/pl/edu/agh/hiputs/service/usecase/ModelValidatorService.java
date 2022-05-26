package pl.edu.agh.hiputs.service.usecase;

import pl.edu.agh.hiputs.exception.ModelValidationException;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;

public interface ModelValidatorService {

    void checkModel(boolean deadEnd, MapFragment mapFragment) throws ModelValidationException;
}
