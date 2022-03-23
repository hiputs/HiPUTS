package pl.edu.agh.exception;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Map;

@ToString
@AllArgsConstructor
public class ModelValidationException extends Exception {

    private Map<String, String> errors;
}
