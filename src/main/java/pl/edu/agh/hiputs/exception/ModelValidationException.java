package pl.edu.agh.hiputs.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public class ModelValidationException extends Exception {

    private Map<String, String> errors;
}
