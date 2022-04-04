package pl.edu.agh.hiputs.scheduler.exception;

public class InsufficientSystemResourcesException extends RuntimeException {

    public InsufficientSystemResourcesException(String message) {
        super(message);
    }
}
