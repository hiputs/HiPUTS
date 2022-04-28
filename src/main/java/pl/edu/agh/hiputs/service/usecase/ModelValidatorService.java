package pl.edu.agh.hiputs.service.usecase;

import pl.edu.agh.hiputs.exception.ModelValidationException;

public interface ModelValidatorService {

    void checkModel(boolean deadEnd) throws ModelValidationException;
}
