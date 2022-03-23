package pl.edu.agh.service;

import pl.edu.agh.exception.ModelValidationException;

public interface ModelValidatorService {

    void checkModel() throws ModelValidationException;
}
